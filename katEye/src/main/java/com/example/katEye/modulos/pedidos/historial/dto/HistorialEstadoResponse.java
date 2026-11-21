package com.example.katEye.modulos.pedidos.historial.dto;

import java.time.Instant;

public record HistorialEstadoResponse(
        String estadoAnterior,
        String estadoNuevo,
        Long actorUsuarioId,
        String motivo,
        Instant fecha) {
}
