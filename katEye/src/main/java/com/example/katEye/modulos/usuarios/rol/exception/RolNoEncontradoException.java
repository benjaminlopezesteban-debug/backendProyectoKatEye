package com.example.katEye.modulos.usuarios.rol.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class RolNoEncontradoException extends RecursoNoEncontradoException {

    public RolNoEncontradoException(Long id) {
        super("Rol no encontrado: " + id);
    }

    public RolNoEncontradoException(String nombre) {
        super("Rol no encontrado: " + nombre);
    }
}
