package com.example.katEye.modulos.pagos.pago.exception;

import com.example.katEye.shared.exception.ReglaNegocioException;

public class PedidoNoPagableException extends ReglaNegocioException {

    public PedidoNoPagableException(String estado) {
        super("El pedido no esta pendiente de pago (estado actual: " + estado + ")");
    }
}
