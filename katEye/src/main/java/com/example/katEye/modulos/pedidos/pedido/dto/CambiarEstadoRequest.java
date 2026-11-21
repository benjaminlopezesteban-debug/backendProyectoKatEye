package com.example.katEye.modulos.pedidos.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(
        @NotBlank String estado,
        @Size(max = 300) String motivo) {
}
