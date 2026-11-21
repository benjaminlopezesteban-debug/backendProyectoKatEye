package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoCommand(
        Long usuarioId,
        String tipoEntrega,
        String contactoNombre,
        String contactoCorreo,
        String contactoTelefono,
        String direccion,
        String regionCodigo,
        String comunaCodigo,
        String metodoPago,
        String idempotencyKey,
        List<LineaPedidoCommand> lineas) {
}
