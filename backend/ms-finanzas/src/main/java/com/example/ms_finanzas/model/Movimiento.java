package com.example.ms_finanzas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Movimiento de la tesoreria: PAGO, INGRESO o GASTO
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;
    private String concepto;

    // En pesos, siempre mayor a 0
    private Long monto;

    // Fecha en milisegundos (epoch), igual que en la app
    private Long fecha;

    private String persona;
    private String descripcion;

    // Opcional: reserva de ms-reservas a la que corresponde el pago
    private Long reservaId;

    // Opcional: ruta/uri de la foto del comprobante
    private String uriComprobante;
}
