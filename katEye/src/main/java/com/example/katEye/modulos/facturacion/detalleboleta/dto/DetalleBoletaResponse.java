package com.example.katEye.modulos.facturacion.detalleboleta.dto;

import java.math.BigDecimal;

public record DetalleBoletaResponse(
        Long pedidoProductoId,
        String descripcion,
        int cantidad,
        BigDecimal precioUnitario) {
}
