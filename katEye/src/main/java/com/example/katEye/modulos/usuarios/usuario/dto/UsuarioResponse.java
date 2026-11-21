package com.example.katEye.modulos.usuarios.usuario.dto;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String regionCodigo,
        String regionNombre,
        String comunaCodigo,
        String comunaNombre,
        String rol,
        boolean activo) {
}
