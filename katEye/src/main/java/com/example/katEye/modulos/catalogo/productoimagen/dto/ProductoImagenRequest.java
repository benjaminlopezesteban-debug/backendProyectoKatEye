package com.example.katEye.modulos.catalogo.productoimagen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoImagenRequest(
        @NotBlank @Size(max = 500) String url,
        @NotNull @Min(0) Short orden) {
}
