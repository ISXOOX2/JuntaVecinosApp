package com.example.ms_reservas.dto;

import java.time.LocalDateTime;

import com.example.ms_reservas.model.HistorialReserva;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialReservaDTO {
    private Long id;
    private Long reservaId;
    private String estadoAnterior;
    private String estadoNuevo;

    private LocalDateTime fechaCambio;

    public HistorialReserva toModel() {
        return new HistorialReserva(id, reservaId, estadoAnterior, estadoNuevo, fechaCambio);
    }

    public static HistorialReservaDTO fromModel(HistorialReserva h) {
        if (h == null) return null;
        return new HistorialReservaDTO(h.getId(), h.getReservaId(), h.getEstadoAnterior(),
                h.getEstadoNuevo(), h.getFechaCambio());
    }
}
