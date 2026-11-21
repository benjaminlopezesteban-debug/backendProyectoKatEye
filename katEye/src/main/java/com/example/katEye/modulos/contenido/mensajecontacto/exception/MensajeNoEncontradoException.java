package com.example.katEye.modulos.contenido.mensajecontacto.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class MensajeNoEncontradoException extends RecursoNoEncontradoException {

    public MensajeNoEncontradoException(Long id) {
        super("Mensaje de contacto no encontrado: " + id);
    }
}
