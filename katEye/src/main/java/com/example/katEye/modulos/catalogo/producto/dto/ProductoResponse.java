package com.example.katEye.modulos.catalogo.producto.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precio,
        BigDecimal precioOferta,
        BigDecimal precioFinal,
        Integer stock,
        Integer stockCritico,
        Long categoriaId,
        String categoriaNombre,
        String imagenUrl,
        boolean activo) {
}
