package com.example.katEye.modulos.contenido.entradablog.exception;

import com.example.katEye.shared.exception.ConflictoException;

public class SlugEntradaDuplicadoException extends ConflictoException {

    public SlugEntradaDuplicadoException(String slug) {
        super("El slug de la entrada ya existe: " + slug);
    }
}
