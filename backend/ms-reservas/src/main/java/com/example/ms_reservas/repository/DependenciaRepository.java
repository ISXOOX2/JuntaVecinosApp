package com.example.ms_reservas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ms_reservas.model.Dependencia;

@Repository
public interface DependenciaRepository extends JpaRepository<Dependencia, Long> {

    Optional<Dependencia> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
