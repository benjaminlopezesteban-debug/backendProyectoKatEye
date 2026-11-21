package com.example.katEye.modulos.catalogo.movimientostock.dto;

public record MovimientoStockResponse(
        Long id,
        Long productoId,
        String productoNombre,
        String tipo,
        Integer cantidad,
        String motivo,
        Long pedidoId,
        Long usuarioId) {
}
