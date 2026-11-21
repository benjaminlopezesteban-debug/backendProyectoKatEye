package com.example.katEye.modulos.facturacion.boleta.dto;

import jakarta.validation.constraints.NotNull;

public record EmitirBoletaRequest(
        @NotNull Long pedidoId,
        String rutReceptor) {
}
