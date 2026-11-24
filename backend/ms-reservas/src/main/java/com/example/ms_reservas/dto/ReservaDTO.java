package com.example.ms_reservas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.ms_reservas.model.Reserva;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private Long id;
    private Long usuarioId;
    private String dependencia;

    private LocalDate fecha;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private String motivo;

    // Solo lectura: el cliente lo puede mandar pero el servidor lo recalcula
    private Integer monto;
    private String estado;

    public Reserva toModel() {
        return new Reserva(id, usuarioId, dependencia, fecha, horaInicio, horaFin, motivo, monto, estado);
    }

    public static ReservaDTO fromModel(Reserva r) {
        if (r == null) return null;
        return new ReservaDTO(r.getId(), r.getUsuarioId(), r.getDependencia(), r.getFecha(),
                r.getHoraInicio(), r.getHoraFin(), r.getMotivo(), r.getMonto(), r.getEstado());
    }
}
