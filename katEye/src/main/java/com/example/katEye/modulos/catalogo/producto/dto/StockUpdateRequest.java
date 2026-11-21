package com.example.katEye.modulos.catalogo.producto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockUpdateRequest(
        @NotNull @Min(0) Integer stock,
        @Size(max = 200) String motivo) {
}
