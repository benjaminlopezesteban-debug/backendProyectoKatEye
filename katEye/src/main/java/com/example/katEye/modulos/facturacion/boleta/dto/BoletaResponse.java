package com.example.katEye.modulos.facturacion.boleta.dto;

import com.example.katEye.modulos.facturacion.detalleboleta.dto.DetalleBoletaResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BoletaResponse(
        Long id,
        String folio,
        String tipoDocumento,
        String rutReceptor,
        Instant fechaEmision,
        BigDecimal montoNeto,
        BigDecimal montoIva,
        BigDecimal total,
        String estado,
        Long ventaId,
        Long pedidoId,
        String numeroPedido,
        List<DetalleBoletaResponse> detalles) {
}
