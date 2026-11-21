package com.example.katEye.modulos.catalogo.producto.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class ProductoNoEncontradoException extends RecursoNoEncontradoException {

    public ProductoNoEncontradoException(Long id) {
        super("Producto no encontrado: " + id);
    }
}
