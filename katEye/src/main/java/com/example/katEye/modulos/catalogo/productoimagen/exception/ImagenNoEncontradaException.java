package com.example.katEye.modulos.catalogo.productoimagen.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class ImagenNoEncontradaException extends RecursoNoEncontradoException {

    public ImagenNoEncontradaException(Long id) {
        super("Imagen no encontrada: " + id);
    }
}
