package com.example.katEye.modulos.carrito.carrito.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(
        Long id,
        Long usuarioId,
        List<CarritoItemResponse> items,
        BigDecimal subtotal,
        BigDecimal ahorro,
        BigDecimal total) {
}
