package com.example.katEye.modulos.catalogo.oferta.exception;

import com.example.katEye.shared.exception.ReglaNegocioException;

public class OfertaInvalidaException extends ReglaNegocioException {

    public OfertaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
