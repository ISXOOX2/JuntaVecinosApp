package com.example.ms_reservas.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_reservas.dto.EstadoDTO;
import com.example.ms_reservas.dto.HistorialReservaDTO;
import com.example.ms_reservas.dto.ReservaDTO;
import com.example.ms_reservas.model.Reserva;
import com.example.ms_reservas.service.ReservaService;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // ?usuarioId=1 filtra las reservas de un vecino
    @GetMapping
    public ResponseEntity<List<ReservaDTO>> listarReservas(@RequestParam(required = false) Long usuarioId) {
        List<ReservaDTO> dtos = reservaService.listar(usuarioId).stream()
                .map(ReservaDTO::fromModel).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> buscarReserva(@PathVariable Long id) {
        return ResponseEntity.ok(ReservaDTO.fromModel(reservaService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<ReservaDTO> crearReserva(@RequestBody ReservaDTO reservaDto) {
        Reserva nueva = reservaService.crear(reservaDto.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaDTO.fromModel(nueva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaDTO> actualizarReserva(@PathVariable Long id, @RequestBody ReservaDTO reservaDto) {
        Reserva actualizada = reservaService.actualizar(id, reservaDto.toModel());
        return ResponseEntity.ok(ReservaDTO.fromModel(actualizada));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReservaDTO> cambiarEstado(@PathVariable Long id, @RequestBody EstadoDTO estadoDto) {
        Reserva reserva = reservaService.cambiarEstado(id, estadoDto.getEstado());
        return ResponseEntity.ok(ReservaDTO.fromModel(reserva));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialReservaDTO>> historial(@PathVariable Long id) {
        List<HistorialReservaDTO> dtos = reservaService.historial(id).stream()
                .map(HistorialReservaDTO::fromModel).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
