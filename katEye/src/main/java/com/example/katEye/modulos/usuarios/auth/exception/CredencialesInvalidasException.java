package com.example.katEye.modulos.usuarios.auth.exception;

import com.example.katEye.shared.exception.NoAutorizadoException;

public class CredencialesInvalidasException extends NoAutorizadoException {

    public CredencialesInvalidasException() {
        super("Credenciales invalidas");
    }
}
