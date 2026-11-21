package com.example.katEye.modulos.facturacion.venta.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record VentaResponse(
        Long id,
        Long pedidoId,
        String numeroPedido,
        Instant fecha,
        BigDecimal montoNeto,
        BigDecimal montoIva,
        BigDecimal total,
        String estado) {
}
