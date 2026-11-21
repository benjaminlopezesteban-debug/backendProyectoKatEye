package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoPagoResponse(
        Long id,
        String numero,
        String estado,
        String metodoPago,
        BigDecimal total,
        Long usuarioId,
        String contactoCorreo,
        List<PedidoProductoResponse> items) {
}
