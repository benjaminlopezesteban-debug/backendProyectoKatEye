package com.example.katEye.modulos.contenido.mensajecontacto.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajeContactoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 100) String correo,
        @NotBlank @Size(max = 500) @JsonAlias("contenido") String comentario) {
}
