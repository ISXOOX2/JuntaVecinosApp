package com.example.ms_finanzas.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_finanzas.dto.MovimientoDTO;
import com.example.ms_finanzas.dto.ResumenDTO;
import com.example.ms_finanzas.model.Movimiento;
import com.example.ms_finanzas.service.MovimientoService;

@RestController
@RequestMapping("/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    // ?tipo=GASTO filtra por tipo
    @GetMapping
    public ResponseEntity<List<MovimientoDTO>> listarMovimientos(@RequestParam(required = false) String tipo) {
        List<MovimientoDTO> dtos = movimientoService.listar(tipo).stream()
                .map(MovimientoDTO::fromModel).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> resumen() {
        return ResponseEntity.ok(movimientoService.resumen());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoDTO> buscarMovimiento(@PathVariable Long id) {
        return ResponseEntity.ok(MovimientoDTO.fromModel(movimientoService.buscarPorId(id)));
    }

    @PostMapping
    public ResponseEntity<MovimientoDTO> crearMovimiento(@RequestBody MovimientoDTO movimientoDto) {
        Movimiento nuevo = movimientoService.guardar(movimientoDto.toModel());
        return ResponseEntity.status(HttpStatus.CREATED).body(MovimientoDTO.fromModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoDTO> actualizarMovimiento(@PathVariable Long id,
            @RequestBody MovimientoDTO movimientoDto) {
        Movimiento actualizado = movimientoService.actualizar(id, movimientoDto.toModel());
        return ResponseEntity.ok(MovimientoDTO.fromModel(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
