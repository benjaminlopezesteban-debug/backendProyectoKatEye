package com.example.katEye.modulos.facturacion.boleta.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class BoletaNoEncontradaException extends RecursoNoEncontradoException {

    public BoletaNoEncontradaException(String referencia) {
        super("Boleta no encontrada: " + referencia);
    }
}
