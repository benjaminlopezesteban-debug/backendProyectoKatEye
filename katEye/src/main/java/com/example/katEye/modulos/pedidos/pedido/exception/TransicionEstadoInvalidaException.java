package com.example.katEye.modulos.pedidos.pedido.exception;

import com.example.katEye.shared.exception.ReglaNegocioException;

public class TransicionEstadoInvalidaException extends ReglaNegocioException {

    public TransicionEstadoInvalidaException(String actual, String destino) {
        super("No se puede pasar del estado " + actual + " a " + destino);
    }
}
