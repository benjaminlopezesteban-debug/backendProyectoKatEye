package com.example.katEye.modulos.contenido.mensajecontacto.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MensajeContactoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Email @Size(max = 100)
        @Pattern(regexp = "(?i)^$|^[A-Za-z0-9._%+-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Solo se permiten correos @duoc.cl, @profesor.duoc.cl o @gmail.com")
        String correo,
        @NotBlank @Size(max = 500) @JsonAlias("contenido") String comentario) {
}
