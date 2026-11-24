package com.example.ms_admin.dto;

import com.example.ms_admin.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que la API devuelve de un usuario: sin el hash de la clave
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;

    // El hash no viaja en el DTO, se completa en el service
    public Usuario toModel() {
        return new Usuario(id, nombre, email, null, rol, activo);
    }

    public static UsuarioDTO fromModel(Usuario u) {
        if (u == null) return null;
        return new UsuarioDTO(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.getActivo());
    }
}
