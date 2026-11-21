package com.example.katEye.modulos.carrito.carrito.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarritoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) @Max(10) Integer cantidad) {
}
