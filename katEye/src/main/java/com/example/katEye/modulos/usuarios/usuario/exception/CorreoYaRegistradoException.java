package com.example.katEye.modulos.usuarios.usuario.exception;

import com.example.katEye.shared.exception.ConflictoException;

public class CorreoYaRegistradoException extends ConflictoException {

    public CorreoYaRegistradoException(String correo) {
        super("El correo ya esta registrado: " + correo);
    }
}
