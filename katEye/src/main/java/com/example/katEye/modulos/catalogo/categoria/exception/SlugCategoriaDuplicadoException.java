package com.example.katEye.modulos.catalogo.categoria.exception;

import com.example.katEye.shared.exception.ConflictoException;

public class SlugCategoriaDuplicadoException extends ConflictoException {

    public SlugCategoriaDuplicadoException(String slug) {
        super("El slug de categoria ya existe: " + slug);
    }
}
