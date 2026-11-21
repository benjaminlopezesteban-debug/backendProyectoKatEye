package com.example.katEye.modulos.usuarios.usuario.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class UsuarioNoEncontradoException extends RecursoNoEncontradoException {

    public UsuarioNoEncontradoException(Long id) {
        super("Usuario no encontrado: " + id);
    }

    public UsuarioNoEncontradoException(String correo) {
        super("Usuario no encontrado: " + correo);
    }
}
