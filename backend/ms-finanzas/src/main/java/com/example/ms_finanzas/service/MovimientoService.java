package com.example.ms_finanzas.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.ms_finanzas.dto.ResumenDTO;
import com.example.ms_finanzas.exception.BadRequestException;
import com.example.ms_finanzas.exception.ResourceNotFoundException;
import com.example.ms_finanzas.model.Movimiento;
import com.example.ms_finanzas.repository.MovimientoRepository;

@Service
public class MovimientoService {

    public static final String PAGO = "PAGO";
    public static final String INGRESO = "INGRESO";
    public static final String GASTO = "GASTO";
    private static final Set<String> TIPOS = Set.of(PAGO, INGRESO, GASTO);

    private final MovimientoRepository movimientoRepository;

    public MovimientoService(MovimientoRepository movimientoRepository) {
        this.movimientoRepository = movimientoRepository;
    }

    public List<Movimiento> listar(String tipo) {
        if (tipo != null && !tipo.isBlank()) {
            return movimientoRepository.findByTipoOrderByFechaDesc(validarTipo(tipo));
        }
        return movimientoRepository.findAllByOrderByFechaDesc();
    }

    public Movimiento buscarPorId(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no existe"));
    }

    public Movimiento guardar(Movimiento movimiento) {
        validar(movimiento);
        movimiento.setId(null);
        return movimientoRepository.save(movimiento);
    }

    public Movimiento actualizar(Long id, Movimiento datos) {
        buscarPorId(id);
        validar(datos);
        datos.setId(id);
        return movimientoRepository.save(datos);
    }

    public void eliminar(Long id) {
        Movimiento movimiento = buscarPorId(id);
        movimientoRepository.delete(movimiento);
    }

    // Ingresos = PAGO + INGRESO; saldo = ingresos - gastos
    public ResumenDTO resumen() {
        List<Movimiento> todos = movimientoRepository.findAll();
        long ingresos = todos.stream()
                .filter(m -> PAGO.equals(m.getTipo()) || INGRESO.equals(m.getTipo()))
                .mapToLong(Movimiento::getMonto).sum();
        long gastos = todos.stream()
                .filter(m -> GASTO.equals(m.getTipo()))
                .mapToLong(Movimiento::getMonto).sum();
        return new ResumenDTO(ingresos, gastos, ingresos - gastos);
    }

    // ---------- validaciones ----------

    private void validar(Movimiento m) {
        m.setTipo(validarTipo(m.getTipo()));
        if (m.getConcepto() == null || m.getConcepto().isBlank()) {
            throw new BadRequestException("El concepto es obligatorio");
        }
        if (m.getMonto() == null || m.getMonto() <= 0) {
            throw new BadRequestException("El monto debe ser mayor a 0");
        }
        // Si no viene fecha se usa la actual
        if (m.getFecha() == null) {
            m.setFecha(System.currentTimeMillis());
        }
        if (m.getPersona() == null) {
            m.setPersona("");
        }
        if (m.getDescripcion() == null) {
            m.setDescripcion("");
        }
    }

    private String validarTipo(String tipo) {
        if (tipo == null || !TIPOS.contains(tipo.trim().toUpperCase())) {
            throw new BadRequestException("Tipo no valido. Use PAGO, INGRESO o GASTO");
        }
        return tipo.trim().toUpperCase();
    }
}
