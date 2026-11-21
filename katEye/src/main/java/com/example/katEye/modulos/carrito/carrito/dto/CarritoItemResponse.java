package com.example.katEye.modulos.carrito.carrito.dto;

import java.math.BigDecimal;

public record CarritoItemResponse(
        Long productoId,
        String nombre,
        BigDecimal precioLista,
        BigDecimal precioUnitario,
        int cantidad,
        BigDecimal subtotal,
        BigDecimal ahorro,
        int stock,
        boolean disponible) {
}
