package com.example.katEye.modulos.contenido.entradablog.dto;

import jakarta.validation.constraints.NotNull;

public record PublicacionRequest(
        @NotNull Boolean publicada) {
}
