package com.example.katEye.modulos.catalogo.productoimagen.controller;

import com.example.katEye.modulos.catalogo.productoimagen.dto.ProductoImagenRequest;
import com.example.katEye.modulos.catalogo.productoimagen.dto.ProductoImagenResponse;
import com.example.katEye.modulos.catalogo.productoimagen.service.ProductoImagenService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos/{productoId}/imagenes")
public class ProductoImagenController {

    private final ProductoImagenService productoImagenService;

    public ProductoImagenController(ProductoImagenService productoImagenService) {
        this.productoImagenService = productoImagenService;
    }

    @GetMapping
    public List<ProductoImagenResponse> listar(@PathVariable Long productoId) {
        return productoImagenService.listar(productoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public ProductoImagenResponse agregar(
            @PathVariable Long productoId,
            @Valid @RequestBody ProductoImagenRequest request) {
        return productoImagenService.agregar(productoId, request);
    }

    @DeleteMapping("/{imagenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public void eliminar(@PathVariable Long productoId, @PathVariable Long imagenId) {
        productoImagenService.eliminar(productoId, imagenId);
    }
}
