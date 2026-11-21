package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;

public record LineaPedidoCommand(
        Long productoId,
        String nombreSnapshot,
        BigDecimal precioLista,
        BigDecimal precioUnitario,
        int cantidad) {
}
