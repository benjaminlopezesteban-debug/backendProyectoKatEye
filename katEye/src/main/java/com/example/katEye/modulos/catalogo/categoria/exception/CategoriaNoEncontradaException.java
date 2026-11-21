package com.example.katEye.modulos.catalogo.categoria.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class CategoriaNoEncontradaException extends RecursoNoEncontradoException {

    public CategoriaNoEncontradaException(Long id) {
        super("Categoria no encontrada: " + id);
    }

    public CategoriaNoEncontradaException(String slug) {
        super("Categoria no encontrada: " + slug);
    }
}
