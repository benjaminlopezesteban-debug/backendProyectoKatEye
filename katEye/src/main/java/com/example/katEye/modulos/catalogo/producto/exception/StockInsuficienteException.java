package com.example.katEye.modulos.catalogo.producto.exception;

import com.example.katEye.shared.exception.ReglaNegocioException;

public class StockInsuficienteException extends ReglaNegocioException {

    public StockInsuficienteException(String producto, int disponible) {
        super("Stock insuficiente para " + producto + " (disponible: " + disponible + ")");
    }
}
