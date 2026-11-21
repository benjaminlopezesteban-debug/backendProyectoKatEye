package com.example.katEye.modulos.pagos.pago.dto;

import java.math.BigDecimal;

public record PagoResponse(
        Long id,
        Long pedidoId,
        String numeroPedido,
        BigDecimal monto,
        String estado,
        String codigoRespuesta,
        String motivo,
        String referenciaExterna) {
}
