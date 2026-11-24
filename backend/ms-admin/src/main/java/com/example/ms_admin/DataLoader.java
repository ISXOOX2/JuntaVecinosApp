package com.example.ms_admin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ms_admin.model.Rol;
import com.example.ms_admin.model.Usuario;
import com.example.ms_admin.repository.RolRepository;
import com.example.ms_admin.repository.UsuarioRepository;
import com.example.ms_admin.service.HashService;

// Carga roles y usuarios de prueba (los mismos de la app). No duplica si ya existen.
@Component
public class DataLoader implements CommandLineRunner {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final HashService hashService;

    DataLoader(RolRepository rolRepository, UsuarioRepository usuarioRepository, HashService hashService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.hashService = hashService;
    }

    @Override
    public void run(String... args) throws Exception {
        crearRol("VECINO", "Vecino");
        crearRol("DIRECTIVA", "Directiva");
        crearRol("TESORERIA", "Tesorería");

        crearUsuario("Vecino Prueba", "vecino@junta.cl", "vecino123", "VECINO");
        crearUsuario("Directiva Prueba", "directiva@junta.cl", "directiva123", "DIRECTIVA");
        crearUsuario("Tesorería Prueba", "tesoreria@junta.cl", "tesoreria123", "TESORERIA");
    }

    private void crearRol(String codigo, String nombre) {
        if (!rolRepository.existsByCodigo(codigo)) {
            rolRepository.save(new Rol(null, codigo, nombre));
        }
    }

    private void crearUsuario(String nombre, String email, String clave, String rol) {
        if (!usuarioRepository.existsByEmail(email)) {
            usuarioRepository.save(new Usuario(null, nombre, email, hashService.sha1(clave), rol, true));
        }
    }
}
