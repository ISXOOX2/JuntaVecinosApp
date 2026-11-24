package com.example.ms_reservas.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ms_reservas.exception.BadRequestException;
import com.example.ms_reservas.exception.ResourceNotFoundException;
import com.example.ms_reservas.model.Dependencia;
import com.example.ms_reservas.model.HistorialReserva;
import com.example.ms_reservas.model.Reserva;
import com.example.ms_reservas.repository.HistorialReservaRepository;
import com.example.ms_reservas.repository.ReservaRepository;

@Service
public class ReservaService {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String APROBADO = "APROBADO";
    public static final String RECHAZADO = "RECHAZADO";
    private static final Set<String> ESTADOS = Set.of(PENDIENTE, APROBADO, RECHAZADO);

    private final ReservaRepository reservaRepository;
    private final HistorialReservaRepository historialRepository;
    private final DependenciaService dependenciaService;

    public ReservaService(ReservaRepository reservaRepository, HistorialReservaRepository historialRepository,
            DependenciaService dependenciaService) {
        this.reservaRepository = reservaRepository;
        this.historialRepository = historialRepository;
        this.dependenciaService = dependenciaService;
    }

    public List<Reserva> listar(Long usuarioId) {
        if (usuarioId != null) {
            return reservaRepository.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuarioId);
        }
        return reservaRepository.findAllByOrderByFechaDescHoraInicioDesc();
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no existe"));
    }

    // Crea la solicitud: valida, revisa choques, calcula monto y queda PENDIENTE
    @Transactional
    public Reserva crear(Reserva reserva) {
        if (reserva.getUsuarioId() == null) {
            throw new BadRequestException("usuarioId es obligatorio");
        }
        Dependencia dependencia = validarDatos(reserva);
        validarSinChoque(reserva, 0L);

        reserva.setId(null);
        reserva.setMonto(calcularMonto(dependencia, reserva));
        reserva.setEstado(PENDIENTE);
        Reserva guardada = reservaRepository.save(reserva);

        registrarHistorial(guardada.getId(), null, PENDIENTE);
        return guardada;
    }

    // Modifica fecha, horario, dependencia o motivo (el estado se cambia con PATCH)
    @Transactional
    public Reserva actualizar(Long id, Reserva datos) {
        Reserva actual = buscarPorId(id);
        actual.setDependencia(datos.getDependencia());
        actual.setFecha(datos.getFecha());
        actual.setHoraInicio(datos.getHoraInicio());
        actual.setHoraFin(datos.getHoraFin());
        actual.setMotivo(datos.getMotivo());
        if (datos.getUsuarioId() != null) {
            actual.setUsuarioId(datos.getUsuarioId());
        }

        Dependencia dependencia = validarDatos(actual);
        if (!RECHAZADO.equals(actual.getEstado())) {
            validarSinChoque(actual, actual.getId());
        }
        actual.setMonto(calcularMonto(dependencia, actual));
        return reservaRepository.save(actual);
    }

    // Aprobar / rechazar / volver a pendiente; guarda el cambio en el historial
    @Transactional
    public Reserva cambiarEstado(Long id, String nuevoEstado) {
        if (nuevoEstado == null || !ESTADOS.contains(nuevoEstado.trim().toUpperCase())) {
            throw new BadRequestException("Estado no valido. Use PENDIENTE, APROBADO o RECHAZADO");
        }
        nuevoEstado = nuevoEstado.trim().toUpperCase();
        Reserva reserva = buscarPorId(id);
        String anterior = reserva.getEstado();
        if (nuevoEstado.equals(anterior)) {
            return reserva;
        }
        // Si se reactiva una rechazada hay que revisar que el horario siga libre
        if (RECHAZADO.equals(anterior)) {
            validarSinChoque(reserva, reserva.getId());
        }
        reserva.setEstado(nuevoEstado);
        Reserva guardada = reservaRepository.save(reserva);
        registrarHistorial(id, anterior, nuevoEstado);
        return guardada;
    }

    @Transactional
    public void eliminar(Long id) {
        Reserva reserva = buscarPorId(id);
        historialRepository.deleteAll(historialRepository.findByReservaId(id));
        reservaRepository.delete(reserva);
    }

    public List<HistorialReserva> historial(Long reservaId) {
        buscarPorId(reservaId);
        return historialRepository.findByReservaIdOrderByFechaCambioAsc(reservaId);
    }

    // ---------- reglas de negocio ----------

    private Dependencia validarDatos(Reserva r) {
        if (r.getDependencia() == null || r.getDependencia().isBlank()) {
            throw new BadRequestException("La dependencia es obligatoria");
        }
        if (r.getFecha() == null || r.getHoraInicio() == null || r.getHoraFin() == null) {
            throw new BadRequestException("fecha, horaInicio y horaFin son obligatorios");
        }
        if (!r.getHoraFin().isAfter(r.getHoraInicio())) {
            throw new BadRequestException("La hora de término debe ser posterior a la de inicio");
        }
        if (r.getMotivo() == null || r.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo es obligatorio");
        }
        return dependenciaService.buscarActivaPorNombre(r.getDependencia());
    }

    private void validarSinChoque(Reserva r, Long idExcluir) {
        // Choca si no está rechazada, no es la misma reserva y los horarios se cruzan
        long choques = reservaRepository.findByDependenciaAndFecha(r.getDependencia(), r.getFecha()).stream()
                .filter(o -> !RECHAZADO.equals(o.getEstado()))
                .filter(o -> !o.getId().equals(idExcluir))
                .filter(o -> o.getHoraInicio().isBefore(r.getHoraFin()) && o.getHoraFin().isAfter(r.getHoraInicio()))
                .count();
        if (choques > 0) {
            throw new BadRequestException("Ese horario ya está ocupado en " + r.getDependencia());
        }
    }

    // Misma formula que la app: tarifa * minutos / 60
    private Integer calcularMonto(Dependencia d, Reserva r) {
        long minutos = Duration.between(r.getHoraInicio(), r.getHoraFin()).toMinutes();
        return (int) (d.getTarifaPorHora() * minutos / 60);
    }

    private void registrarHistorial(Long reservaId, String anterior, String nuevo) {
        historialRepository.save(new HistorialReserva(null, reservaId, anterior, nuevo, LocalDateTime.now()));
    }
}
