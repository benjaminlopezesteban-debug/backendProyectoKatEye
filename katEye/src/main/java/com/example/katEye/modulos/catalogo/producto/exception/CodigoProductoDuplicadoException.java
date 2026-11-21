package com.example.katEye.modulos.catalogo.producto.exception;

import com.example.katEye.shared.exception.ConflictoException;

public class CodigoProductoDuplicadoException extends ConflictoException {

    public CodigoProductoDuplicadoException(String codigo) {
        super("El codigo de producto ya existe: " + codigo);
    }
}
