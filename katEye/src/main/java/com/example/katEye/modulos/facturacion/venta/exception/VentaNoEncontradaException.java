package com.example.katEye.modulos.facturacion.venta.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class VentaNoEncontradaException extends RecursoNoEncontradoException {

    public VentaNoEncontradaException(String referencia) {
        super("Venta no encontrada: " + referencia);
    }
}
