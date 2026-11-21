package com.example.katEye.modulos.pedidos.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeguimientoRequest(
        @NotBlank @Size(max = 100) String transportista,
        @NotBlank @Size(max = 60) String codigoSeguimiento) {
}
