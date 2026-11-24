package com.example.ms_reservas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ms_reservas.exception.BadRequestException;
import com.example.ms_reservas.model.Dependencia;
import com.example.ms_reservas.repository.DependenciaRepository;

@Service
public class DependenciaService {

    private final DependenciaRepository dependenciaRepository;

    public DependenciaService(DependenciaRepository dependenciaRepository) {
        this.dependenciaRepository = dependenciaRepository;
    }

    public List<Dependencia> listar() {
        return dependenciaRepository.findAll();
    }

    // Busca por nombre y verifica que se pueda arrendar
    public Dependencia buscarActivaPorNombre(String nombre) {
        Dependencia dependencia = dependenciaRepository.findByNombre(nombre)
                .orElseThrow(() -> new BadRequestException("La dependencia no existe: " + nombre));
        if (Boolean.FALSE.equals(dependencia.getActiva())) {
            throw new BadRequestException("La dependencia no está disponible: " + nombre);
        }
        return dependencia;
    }
}
