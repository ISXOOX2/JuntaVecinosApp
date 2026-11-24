package com.example.ms_admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ms_admin.dto.LoginDTO;
import com.example.ms_admin.dto.RegistroDTO;
import com.example.ms_admin.dto.UsuarioDTO;
import com.example.ms_admin.model.Usuario;
import com.example.ms_admin.service.UsuarioService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody RegistroDTO datos) {
        Usuario nuevo = usuarioService.registrar(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioDTO.fromModel(nuevo));
    }

    // Devuelve el usuario sin hash; si falla responde 401
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(@RequestBody LoginDTO datos) {
        Usuario usuario = usuarioService.login(datos);
        return ResponseEntity.ok(UsuarioDTO.fromModel(usuario));
    }
}
