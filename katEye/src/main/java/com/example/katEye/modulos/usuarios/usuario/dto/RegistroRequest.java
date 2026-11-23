package com.example.katEye.modulos.usuarios.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 100)
        @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com")
        String correo,
        @NotBlank @Size(min = 4, max = 10) String password,
        @Size(max = 20) String telefono,
        @NotBlank @Size(max = 10) String regionCodigo,
        @NotBlank @Size(max = 10) String comunaCodigo) {
}
