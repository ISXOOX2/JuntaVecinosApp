package com.example.ms_reservas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ms_reservas.model.Dependencia;
import com.example.ms_reservas.repository.DependenciaRepository;

// Carga las 3 dependencias de la app (mismos nombres y tarifas). No duplica.
@Component
public class DataLoader implements CommandLineRunner {
    private final DependenciaRepository dependenciaRepository;

    DataLoader(DependenciaRepository dependenciaRepository) {
        this.dependenciaRepository = dependenciaRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        crear("Salón multiuso", "Espacio amplio para eventos y reuniones grandes", 12000);
        crear("Sala de reuniones", "Sala pequeña para reuniones y talleres", 6000);
        crear("Cancha techada", "Cancha para actividades deportivas", 9000);
    }

    private void crear(String nombre, String descripcion, int tarifa) {
        if (!dependenciaRepository.existsByNombre(nombre)) {
            dependenciaRepository.save(new Dependencia(null, nombre, descripcion, tarifa, true));
        }
    }
}
