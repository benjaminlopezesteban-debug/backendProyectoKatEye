package com.example.katEye.modulos.carrito.carritoitem.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class ItemCarritoNoEncontradoException extends RecursoNoEncontradoException {

    public ItemCarritoNoEncontradoException(Long productoId) {
        super("El carrito no contiene el producto: " + productoId);
    }
}
