package com.example.katEye.modulos.contenido.mensajecontacto.dto;

import java.time.Instant;

public record MensajeContactoResponse(
        Long id,
        String nombre,
        String correo,
        String comentario,
        String estado,
        Long respondidoPor,
        Instant creadoEn) {
}
