package com.example.ms_reservas.dto;

import com.example.ms_reservas.model.Dependencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DependenciaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer tarifaPorHora;
    private Boolean activa;

    public Dependencia toModel() {
        return new Dependencia(id, nombre, descripcion, tarifaPorHora, activa);
    }

    public static DependenciaDTO fromModel(Dependencia d) {
        if (d == null) return null;
        return new DependenciaDTO(d.getId(), d.getNombre(), d.getDescripcion(), d.getTarifaPorHora(), d.getActiva());
    }
}
