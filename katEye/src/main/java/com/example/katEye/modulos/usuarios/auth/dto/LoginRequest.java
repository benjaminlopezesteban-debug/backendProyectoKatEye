package com.example.katEye.modulos.usuarios.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank @Email
        @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com|kateye\\.cl)$",
                message = "Correo no permitido para el login")
        String correo,
        @NotBlank String password) {
}
