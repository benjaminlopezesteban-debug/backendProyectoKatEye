package com.example.katEye.modulos.catalogo.oferta.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class OfertaNoEncontradaException extends RecursoNoEncontradoException {

    public OfertaNoEncontradaException(Long id) {
        super("Oferta no encontrada: " + id);
    }
}
