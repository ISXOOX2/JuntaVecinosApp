package com.example.ms_admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos para registrar un vecino nuevo
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {
    private String nombre;
    private String email;
    private String password;
}
