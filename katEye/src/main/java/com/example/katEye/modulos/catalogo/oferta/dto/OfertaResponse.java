package com.example.katEye.modulos.catalogo.oferta.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OfertaResponse(
        Long id,
        Long productoId,
        String productoNombre,
        BigDecimal precioAnterior,
        BigDecimal precioOferta,
        Instant desde,
        Instant hasta,
        boolean vigente) {
}
