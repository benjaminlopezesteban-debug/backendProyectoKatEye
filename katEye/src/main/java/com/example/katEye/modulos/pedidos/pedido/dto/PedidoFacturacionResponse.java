package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoFacturacionResponse(
        Long id,
        String numero,
        Long usuarioId,
        String contactoCorreo,
        BigDecimal total,
        List<PedidoProductoResponse> items) {
}
