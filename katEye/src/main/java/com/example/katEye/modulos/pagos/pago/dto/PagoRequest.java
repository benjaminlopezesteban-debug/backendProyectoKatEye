package com.example.katEye.modulos.pagos.pago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PagoRequest(
        @NotNull Long pedidoId,
        @NotBlank
        @Pattern(regexp = "TARJETA|TRANSFERENCIA", message = "debe ser TARJETA o TRANSFERENCIA")
        String metodo) {
}
