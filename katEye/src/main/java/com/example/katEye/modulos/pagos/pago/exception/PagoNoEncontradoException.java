package com.example.katEye.modulos.pagos.pago.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class PagoNoEncontradoException extends RecursoNoEncontradoException {

    public PagoNoEncontradoException(Long id) {
        super("Pago no encontrado: " + id);
    }
}
