package com.example.katEye.modulos.pedidos.pedido.exception;

import com.example.katEye.shared.exception.RecursoNoEncontradoException;

public class PedidoNoEncontradoException extends RecursoNoEncontradoException {

    public PedidoNoEncontradoException(String numero) {
        super("Pedido no encontrado: " + numero);
    }

    public PedidoNoEncontradoException(Long id) {
        super("Pedido no encontrado: " + id);
    }
}
