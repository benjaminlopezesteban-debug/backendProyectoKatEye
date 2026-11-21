package com.example.katEye.modulos.usuarios.intentoacceso.dto;

import java.time.Instant;

public record IntentoAccesoResponse(
        Long id,
        Long usuarioId,
        String tipo,
        String correo,
        String ip,
        boolean exitoso,
        Instant fecha) {
}
