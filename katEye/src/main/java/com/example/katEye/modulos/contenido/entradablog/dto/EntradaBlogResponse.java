package com.example.katEye.modulos.contenido.entradablog.dto;

import java.time.Instant;
import java.util.List;

public record EntradaBlogResponse(
        Long id,
        String slug,
        String titulo,
        String resumen,
        String imagen,
        List<BloqueBlog> contenido,
        boolean publicada,
        Instant publicadaEn,
        Long autorUsuarioId) {
}
