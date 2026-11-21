package com.example.katEye.modulos.usuarios.recuperacion.exception;

import com.example.katEye.shared.exception.SolicitudInvalidaException;

public class TokenInvalidoException extends SolicitudInvalidaException {

    public TokenInvalidoException() {
        super("Token de recuperacion invalido o expirado");
    }
}
