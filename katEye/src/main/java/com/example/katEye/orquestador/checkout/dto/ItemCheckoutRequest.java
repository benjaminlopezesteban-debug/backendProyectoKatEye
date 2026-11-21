package com.example.katEye.orquestador.checkout.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCheckoutRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) @Max(10) Integer cantidad) {
}
