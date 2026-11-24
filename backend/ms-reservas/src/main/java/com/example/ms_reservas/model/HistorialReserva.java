package com.example.ms_reservas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cada cambio de estado de una reserva queda registrado aqui
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reservaId;

    // null cuando la reserva recien se crea
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
}
