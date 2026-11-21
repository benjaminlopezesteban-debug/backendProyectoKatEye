package com.example.katEye.modulos.catalogo.productoimagen.service;

import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import com.example.katEye.modulos.catalogo.productoimagen.dto.ProductoImagenRequest;
import com.example.katEye.modulos.catalogo.productoimagen.dto.ProductoImagenResponse;
import com.example.katEye.modulos.catalogo.productoimagen.entity.ProductoImagen;
import com.example.katEye.modulos.catalogo.productoimagen.exception.ImagenNoEncontradaException;
import com.example.katEye.modulos.catalogo.productoimagen.exception.OrdenImagenDuplicadoException;
import com.example.katEye.modulos.catalogo.productoimagen.repository.ProductoImagenRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoImagenService {

    private final ProductoImagenRepository productoImagenRepository;
    private final ProductoService productoService;

    public ProductoImagenService(
            ProductoImagenRepository productoImagenRepository,
            ProductoService productoService) {
        this.productoImagenRepository = productoImagenRepository;
        this.productoService = productoService;
    }

    @Transactional(readOnly = true)
    public List<ProductoImagenResponse> listar(Long productoId) {
        productoService.obtenerEntidad(productoId);
        return productoImagenRepository.findByProductoIdOrderByOrdenAsc(productoId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public ProductoImagenResponse agregar(Long productoId, ProductoImagenRequest request) {
        Producto producto = productoService.obtenerEntidad(productoId);
        if (productoImagenRepository.existsByProductoIdAndOrden(productoId, request.orden())) {
            throw new OrdenImagenDuplicadoException(productoId, request.orden());
        }
        ProductoImagen imagen = new ProductoImagen(producto, request.url().trim(), request.orden());
        return aResponse(productoImagenRepository.save(imagen));
    }

    @Transactional
    public void eliminar(Long productoId, Long imagenId) {
        ProductoImagen imagen = productoImagenRepository.findByIdAndProductoId(imagenId, productoId)
                .orElseThrow(() -> new ImagenNoEncontradaException(imagenId));
        productoImagenRepository.delete(imagen);
    }

    private ProductoImagenResponse aResponse(ProductoImagen imagen) {
        return new ProductoImagenResponse(
                imagen.getId(),
                imagen.getProducto().getId(),
                imagen.getUrl(),
                imagen.getOrden());
    }
}
