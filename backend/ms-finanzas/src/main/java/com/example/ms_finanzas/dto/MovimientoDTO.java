package com.example.ms_finanzas.dto;

import com.example.ms_finanzas.model.Movimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoDTO {
    private Long id;
    private String tipo;
    private String concepto;
    private Long monto;
    private Long fecha;
    private String persona;
    private String descripcion;
    private Long reservaId;
    private String uriComprobante;

    public Movimiento toModel() {
        return new Movimiento(id, tipo, concepto, monto, fecha, persona, descripcion, reservaId, uriComprobante);
    }

    public static MovimientoDTO fromModel(Movimiento m) {
        if (m == null) return null;
        return new MovimientoDTO(m.getId(), m.getTipo(), m.getConcepto(), m.getMonto(), m.getFecha(),
                m.getPersona(), m.getDescripcion(), m.getReservaId(), m.getUriComprobante());
    }
}
