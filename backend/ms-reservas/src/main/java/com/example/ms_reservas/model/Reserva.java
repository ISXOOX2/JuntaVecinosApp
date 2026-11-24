package com.example.ms_reservas.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Solicitud de arriendo de una dependencia (en la app se llama Arriendo)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id del usuario en ms-admin
    private Long usuarioId;

    // Nombre de la dependencia, igual que en la app
    private String dependencia;

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;

    // Lo calcula el servidor: tarifaPorHora * minutos / 60
    private Integer monto;

    // PENDIENTE, APROBADO o RECHAZADO
    private String estado;
}
