package com.example.ms_reservas.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_reservas.dto.DependenciaDTO;
import com.example.ms_reservas.service.DependenciaService;

@RestController
@RequestMapping("/dependencias")
public class DependenciaController {

    private final DependenciaService dependenciaService;

    public DependenciaController(DependenciaService dependenciaService) {
        this.dependenciaService = dependenciaService;
    }

    @GetMapping
    public ResponseEntity<List<DependenciaDTO>> listarDependencias() {
        List<DependenciaDTO> dtos = dependenciaService.listar().stream()
                .map(DependenciaDTO::fromModel).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
