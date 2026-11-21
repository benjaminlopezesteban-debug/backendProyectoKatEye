package com.example.katEye.modulos.catalogo.productoimagen.exception;

import com.example.katEye.shared.exception.ConflictoException;

public class OrdenImagenDuplicadoException extends ConflictoException {

    public OrdenImagenDuplicadoException(Long productoId, Short orden) {
        super("El producto " + productoId + " ya tiene una imagen en el orden " + orden);
    }
}
