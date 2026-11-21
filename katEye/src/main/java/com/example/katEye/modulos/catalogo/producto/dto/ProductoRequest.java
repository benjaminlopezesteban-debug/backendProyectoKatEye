package com.example.katEye.modulos.catalogo.producto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank @Size(min = 3, max = 20) String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @DecimalMin("0") BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @Min(0) Integer stockCritico,
        @NotNull Long categoriaId,
        Boolean activo) {
}
