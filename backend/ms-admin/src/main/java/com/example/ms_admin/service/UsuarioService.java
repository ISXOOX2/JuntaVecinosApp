package com.example.ms_admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ms_admin.dto.LoginDTO;
import com.example.ms_admin.dto.RegistroDTO;
import com.example.ms_admin.exception.BadRequestException;
import com.example.ms_admin.exception.ResourceNotFoundException;
import com.example.ms_admin.model.Usuario;
import com.example.ms_admin.repository.RolRepository;
import com.example.ms_admin.repository.UsuarioRepository;

@Service
public class UsuarioService {

    public static final String ROL_POR_DEFECTO = "VECINO";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final HashService hashService;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, HashService hashService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.hashService = hashService;
    }

    // Registro: quien se registra desde la app siempre es VECINO
    public Usuario registrar(RegistroDTO datos) {
        if (datos == null || vacio(datos.getNombre()) || vacio(datos.getEmail()) || vacio(datos.getPassword())) {
            throw new BadRequestException("Nombre, email y password son obligatorios");
        }
        if (datos.getPassword().length() < 6) {
            throw new BadRequestException("La password debe tener al menos 6 caracteres");
        }
        String email = normalizarEmail(datos.getEmail());
        if (!email.contains("@")) {
            throw new BadRequestException("El email no es valido");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Este correo ya está registrado");
        }
        Usuario usuario = new Usuario(null, datos.getNombre().trim(), email,
                hashService.sha1(datos.getPassword()), ROL_POR_DEFECTO, true);
        return usuarioRepository.save(usuario);
    }

    // Login: compara el hash de la clave recibida con el guardado
    public Usuario login(LoginDTO datos) {
        if (datos == null || vacio(datos.getEmail()) || vacio(datos.getPassword())) {
            throw new BadRequestException("Email y password son obligatorios");
        }
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(datos.getEmail()))
                .orElseThrow(() -> new BadRequestException("Correo o clave incorrectos"));
        if (!usuario.getPasswordHash().equals(hashService.sha1(datos.getPassword()))) {
            throw new BadRequestException("Correo o clave incorrectos");
        }
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("El usuario está desactivado");
        }
        return usuario;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no existe"));
    }

    // Actualiza nombre, email, rol y activo (la clave no se cambia aqui)
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario actual = buscarPorId(id);
        if (!vacio(datos.getNombre())) {
            actual.setNombre(datos.getNombre().trim());
        }
        if (!vacio(datos.getEmail())) {
            String email = normalizarEmail(datos.getEmail());
            if (!email.equals(actual.getEmail()) && usuarioRepository.existsByEmail(email)) {
                throw new BadRequestException("Este correo ya está registrado");
            }
            actual.setEmail(email);
        }
        if (!vacio(datos.getRol())) {
            if (!rolRepository.existsByCodigo(datos.getRol())) {
                throw new BadRequestException("Rol no valido: " + datos.getRol());
            }
            actual.setRol(datos.getRol());
        }
        if (datos.getActivo() != null) {
            actual.setActivo(datos.getActivo());
        }
        return usuarioRepository.save(actual);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
