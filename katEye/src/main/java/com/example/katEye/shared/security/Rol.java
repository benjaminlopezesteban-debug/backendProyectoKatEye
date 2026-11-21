package com.example.katEye.shared.security;

public enum Rol {
    ADMINISTRADOR,
    VENDEDOR,
    CLIENTE;

    public String autoridad() {
        return "ROLE_" + name();
    }
}
