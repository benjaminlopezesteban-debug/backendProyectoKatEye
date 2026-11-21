package com.example.katEye.modulos.ubicacion.comuna.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class ComunaNoEncontradaException extends RecursoNoEncontradoException {

    public ComunaNoEncontradaException(String codigo) {
        super("Comuna no encontrada: " + codigo);
    }
}
