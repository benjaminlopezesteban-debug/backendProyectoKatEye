package com.example.katEye.modulos.catalogo.categoria.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank
        @Size(max = 60)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "debe ser un slug en minusculas")
        String slug,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @Min(0) Short orden) {
}
