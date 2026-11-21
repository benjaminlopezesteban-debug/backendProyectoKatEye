package com.example.katEye.modulos.ubicacion.region.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class RegionNoEncontradaException extends RecursoNoEncontradoException {

    public RegionNoEncontradaException(String codigo) {
        super("Region no encontrada: " + codigo);
    }
}
