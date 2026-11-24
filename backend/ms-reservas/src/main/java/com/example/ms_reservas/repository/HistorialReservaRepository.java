package com.example.ms_reservas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ms_reservas.model.HistorialReserva;

@Repository
public interface HistorialReservaRepository extends JpaRepository<HistorialReserva, Long> {

    List<HistorialReserva> findByReservaIdOrderByFechaCambioAsc(Long reservaId);

    List<HistorialReserva> findByReservaId(Long reservaId);
}
