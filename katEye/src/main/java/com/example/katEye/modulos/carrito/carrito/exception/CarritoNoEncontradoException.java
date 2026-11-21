package com.example.katEye.modulos.carrito.carrito.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class CarritoNoEncontradoException extends RecursoNoEncontradoException {

    public CarritoNoEncontradoException(Long id) {
        super("Carrito no encontrado: " + id);
    }
}
