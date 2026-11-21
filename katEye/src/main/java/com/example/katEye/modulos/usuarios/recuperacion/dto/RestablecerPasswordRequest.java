package com.example.katEye.modulos.usuarios.recuperacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 4, max = 72) String password) {
}
