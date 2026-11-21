package com.example.katEye.shared.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int estado,
        String error,
        String mensaje,
        String ruta) {
}
