package com.example.katEye.modulos.usuarios.usuario.controller;

import com.example.katEye.modulos.usuarios.usuario.dto.AsignarRolRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioResponse;
import com.example.katEye.modulos.usuarios.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioResponse obtenerPerfil() {
        return usuarioService.obtenerPerfil();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse asignarRol(
            @PathVariable Long id,
            @Valid @RequestBody AsignarRolRequest request) {
        return usuarioService.asignarRol(id, request.rolId());
    }
}
