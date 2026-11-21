package com.example.katEye.modulos.contenido.mensajecontacto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarEstadoMensajeRequest(
        @NotBlank
        @Pattern(regexp = "LEIDO|RESPONDIDO", message = "debe ser LEIDO o RESPONDIDO")
        String estado) {
}
