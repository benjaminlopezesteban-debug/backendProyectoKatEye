package com.example.katEye.modulos.contenido.mensajecontacto.entity;

public enum EstadoMensaje {
    NUEVO,
    LEIDO,
    RESPONDIDO;

    public boolean puedePasarA(EstadoMensaje destino) {
        return switch (this) {
            case NUEVO -> destino == LEIDO || destino == RESPONDIDO;
            case LEIDO -> destino == RESPONDIDO;
            case RESPONDIDO -> false;
        };
    }
}
