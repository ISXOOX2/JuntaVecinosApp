package com.example.ms_finanzas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Totales para la pantalla de transparencia / reporte
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenDTO {
    // PAGO + INGRESO
    private Long totalIngresos;
    private Long totalGastos;
    private Long saldo;
}
