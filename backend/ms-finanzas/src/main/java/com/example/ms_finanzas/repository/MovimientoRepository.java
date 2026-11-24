package com.example.ms_finanzas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ms_finanzas.model.Movimiento;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findAllByOrderByFechaDesc();

    List<Movimiento> findByTipoOrderByFechaDesc(String tipo);

}
