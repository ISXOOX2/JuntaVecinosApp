package com.example.ms_reservas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ms_reservas.model.Reserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    List<Reserva> findAllByOrderByFechaDescHoraInicioDesc();

    // Reservas de una dependencia en una fecha (el choque se revisa en el Service)
    List<Reserva> findByDependenciaAndFecha(String dependencia, LocalDate fecha);
}
