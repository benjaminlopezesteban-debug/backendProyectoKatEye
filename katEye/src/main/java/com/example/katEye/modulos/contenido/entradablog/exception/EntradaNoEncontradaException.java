package com.example.katEye.modulos.contenido.entradablog.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class EntradaNoEncontradaException extends RecursoNoEncontradoException {

    public EntradaNoEncontradaException(String referencia) {
        super("Entrada de blog no encontrada: " + referencia);
    }
}
