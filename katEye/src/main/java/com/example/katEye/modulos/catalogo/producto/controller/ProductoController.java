package com.example.katEye.modulos.catalogo.producto.controller;

import com.example.katEye.modulos.catalogo.producto.dto.ProductoRequest;
import com.example.katEye.modulos.catalogo.producto.dto.ProductoResponse;
import com.example.katEye.modulos.catalogo.producto.dto.StockUpdateRequest;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax) {
        return productoService.listar(categoriaId, precioMin, precioMax);
    }

    @GetMapping("/stock-critico")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public List<ProductoResponse> stockCritico() {
        return productoService.stockCritico();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @GetMapping("/{id}/relacionados")
    public List<ProductoResponse> relacionados(@PathVariable Long id) {
        return productoService.relacionados(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public ProductoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public ProductoResponse actualizarStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return productoService.actualizarStock(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
