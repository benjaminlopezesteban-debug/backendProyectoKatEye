package com.example.katEye.orquestador.checkout.dto;

import java.math.BigDecimal;

public record CheckoutResponse(
        Long pedidoId,
        String numero,
        BigDecimal total,
        String estado) {
}
