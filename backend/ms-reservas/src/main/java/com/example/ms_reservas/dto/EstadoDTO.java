package com.example.ms_reservas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de PATCH /reservas/{id}/estado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDTO {
    private String estado;
}
