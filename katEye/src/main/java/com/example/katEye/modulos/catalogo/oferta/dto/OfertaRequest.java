package com.example.katEye.modulos.catalogo.oferta.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record OfertaRequest(
        @NotNull Long productoId,
        @NotNull @DecimalMin("0") BigDecimal precioAnterior,
        @NotNull @DecimalMin("0") BigDecimal precioOferta,
        @NotNull Instant desde,
        @NotNull Instant hasta) {
}
