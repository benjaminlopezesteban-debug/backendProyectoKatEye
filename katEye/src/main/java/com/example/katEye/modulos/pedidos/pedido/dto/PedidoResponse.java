package com.example.katEye.modulos.pedidos.pedido.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numero,
        Long usuarioId,
        String tipoEntrega,
        String contactoNombre,
        String contactoCorreo,
        String contactoTelefono,
        String direccion,
        String regionCodigo,
        String comunaCodigo,
        String metodoPago,
        String estado,
        BigDecimal subtotal,
        BigDecimal ahorro,
        BigDecimal costoDespacho,
        BigDecimal total,
        String transportista,
        String codigoSeguimiento,
        java.time.Instant expiraEn,
        List<PedidoProductoResponse> items) {
}
