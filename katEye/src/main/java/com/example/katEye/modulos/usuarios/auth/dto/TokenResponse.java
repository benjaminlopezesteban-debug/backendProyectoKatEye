package com.example.katEye.modulos.usuarios.auth.dto;

import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioResponse;

public record TokenResponse(
        String token,
        String tipo,
        long expiraEn,
        UsuarioResponse usuario) {
}
