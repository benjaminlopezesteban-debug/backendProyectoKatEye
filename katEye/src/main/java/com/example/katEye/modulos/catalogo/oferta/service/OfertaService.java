package com.example.katEye.modulos.catalogo.oferta.service;

import com.example.katEye.modulos.catalogo.oferta.dto.OfertaRequest;
import com.example.katEye.modulos.catalogo.oferta.dto.OfertaResponse;
import com.example.katEye.modulos.catalogo.oferta.entity.Oferta;
import com.example.katEye.modulos.catalogo.oferta.exception.OfertaInvalidaException;
import com.example.katEye.modulos.catalogo.oferta.exception.OfertaNoEncontradaException;
import com.example.katEye.modulos.catalogo.oferta.repository.OfertaRepository;
import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final ProductoService productoService;

    public OfertaService(OfertaRepository ofertaRepository, ProductoService productoService) {
        this.ofertaRepository = ofertaRepository;
        this.productoService = productoService;
    }

    @Transactional(readOnly = true)
    public List<OfertaResponse> listarVigentes() {
        Instant ahora = Instant.now();
        return ofertaRepository.findVigentes(ahora).stream()
                .map(oferta -> aResponse(oferta, ahora))
                .toList();
    }

    @Transactional
    public OfertaResponse crear(OfertaRequest request) {
        validar(request);
        Producto producto = productoService.obtenerEntidad(request.productoId());
        Oferta oferta = new Oferta(
                producto,
                request.precioAnterior(),
                request.precioOferta(),
                request.desde(),
                request.hasta());
        return aResponse(ofertaRepository.save(oferta), Instant.now());
    }

    @Transactional
    public OfertaResponse actualizar(Long id, OfertaRequest request) {
        validar(request);
        Oferta oferta = obtenerEntidad(id);
        Producto producto = productoService.obtenerEntidad(request.productoId());
        oferta.actualizar(
                producto,
                request.precioAnterior(),
                request.precioOferta(),
                request.desde(),
                request.hasta());
        return aResponse(oferta, Instant.now());
    }

    @Transactional
    public void eliminar(Long id) {
        ofertaRepository.delete(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public Optional<Oferta> obtenerVigentePorProducto(Long productoId) {
        Instant ahora = Instant.now();
        return ofertaRepository
                .findFirstByProductoIdAndDesdeLessThanEqualAndHastaAfterOrderByDesdeDesc(
                        productoId, ahora, ahora);
    }

    private Oferta obtenerEntidad(Long id) {
        return ofertaRepository.findById(id)
                .orElseThrow(() -> new OfertaNoEncontradaException(id));
    }

    private void validar(OfertaRequest request) {
        if (!request.hasta().isAfter(request.desde())) {
            throw new OfertaInvalidaException("La fecha de fin debe ser posterior a la de inicio");
        }
        if (request.precioOferta().compareTo(request.precioAnterior()) > 0) {
            throw new OfertaInvalidaException("El precio de oferta no puede superar al precio anterior");
        }
    }

    private OfertaResponse aResponse(Oferta oferta, Instant ahora) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getProducto().getId(),
                oferta.getProducto().getNombre(),
                oferta.getPrecioAnterior(),
                oferta.getPrecioOferta(),
                oferta.getDesde(),
                oferta.getHasta(),
                oferta.estaVigente(ahora));
    }
}
