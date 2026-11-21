package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;

public record PedidoProductoResponse(
        Long id,
        Long productoId,
        String nombreSnapshot,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal subtotal) {
}
