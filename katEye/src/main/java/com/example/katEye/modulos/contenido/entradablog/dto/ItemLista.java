package com.example.katEye.modulos.contenido.entradablog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemLista(
        @NotBlank @Size(max = 200) String etiqueta,
        @NotBlank @Size(max = 2000) String texto) {
}
