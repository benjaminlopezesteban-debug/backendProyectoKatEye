package com.example.katEye.modulos.catalogo.producto.service;

import com.example.katEye.modulos.catalogo.categoria.entity.Categoria;
import com.example.katEye.modulos.catalogo.categoria.service.CategoriaService;
import com.example.katEye.modulos.catalogo.movimientostock.entity.TipoMovimiento;
import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import com.example.katEye.modulos.catalogo.oferta.entity.Oferta;
import com.example.katEye.modulos.catalogo.oferta.repository.OfertaRepository;
import com.example.katEye.modulos.catalogo.producto.dto.ProductoRequest;
import com.example.katEye.modulos.catalogo.producto.dto.ProductoResponse;
import com.example.katEye.modulos.catalogo.producto.dto.StockUpdateRequest;
import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.exception.CodigoProductoDuplicadoException;
import com.example.katEye.modulos.catalogo.producto.exception.ProductoNoEncontradoException;
import com.example.katEye.modulos.catalogo.producto.exception.StockInsuficienteException;
import com.example.katEye.modulos.catalogo.producto.repository.ProductoRepository;
import com.example.katEye.modulos.catalogo.productoimagen.entity.ProductoImagen;
import com.example.katEye.modulos.catalogo.productoimagen.repository.ProductoImagenRepository;
import com.example.katEye.shared.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final OfertaRepository ofertaRepository;
    private final MovimientoStockService movimientoStockService;
    private final ProductoImagenRepository productoImagenRepository;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaService categoriaService,
            OfertaRepository ofertaRepository,
            MovimientoStockService movimientoStockService,
            ProductoImagenRepository productoImagenRepository) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.ofertaRepository = ofertaRepository;
        this.movimientoStockService = movimientoStockService;
        this.productoImagenRepository = productoImagenRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, BigDecimal precioMin, BigDecimal precioMax) {
        return productoRepository.buscar(categoriaId, precioMin, precioMax).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return aResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> relacionados(Long id) {
        Producto producto = obtenerEntidad(id);
        return productoRepository
                .findTop8ByCategoriaIdAndIdNotAndActivoTrueOrderByNombreAsc(
                        producto.getCategoria().getId(), id)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> stockCritico() {
        return productoRepository.listarStockCritico().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (productoRepository.existsByCodigo(codigo)) {
            throw new CodigoProductoDuplicadoException(codigo);
        }
        Categoria categoria = categoriaService.obtenerEntidad(request.categoriaId());
        Producto producto = new Producto(
                codigo,
                request.nombre().trim(),
                request.descripcion(),
                request.precio(),
                request.stock(),
                request.stockCritico(),
                categoria,
                request.activo() == null || request.activo());
        return aResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = obtenerEntidad(id);
        String codigo = normalizarCodigo(request.codigo());
        if (productoRepository.existsByCodigoAndIdNot(codigo, id)) {
            throw new CodigoProductoDuplicadoException(codigo);
        }
        Categoria categoria = categoriaService.obtenerEntidad(request.categoriaId());
        producto.actualizar(
                codigo,
                request.nombre().trim(),
                request.descripcion(),
                request.precio(),
                request.stockCritico(),
                categoria,
                request.activo() == null || request.activo());
        sincronizarStock(producto, request.stock(), "Actualizacion de producto");
        return aResponse(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        obtenerEntidad(id).desactivar();
    }

    @Transactional
    public ProductoResponse actualizarStock(Long id, StockUpdateRequest request) {
        Producto producto = obtenerEntidad(id);
        sincronizarStock(producto, request.stock(), request.motivo());
        return aResponse(producto);
    }

    private void sincronizarStock(Producto producto, Integer nuevoStock, String motivo) {
        int stockAnterior = producto.getStock();
        producto.cambiarStock(nuevoStock);
        int diferencia = nuevoStock - stockAnterior;
        if (diferencia != 0) {
            movimientoStockService.registrar(
                    producto,
                    TipoMovimiento.AJUSTE,
                    Math.abs(diferencia),
                    motivo,
                    null,
                    SecurityUtils.usuarioActualId().orElse(null));
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerPrecioVigente(Long id) {
        Producto producto = obtenerEntidad(id);
        Instant ahora = Instant.now();
        return ofertaRepository
                .findFirstByProductoIdAndDesdeLessThanEqualAndHastaAfterOrderByDesdeDesc(id, ahora, ahora)
                .map(Oferta::getPrecioOferta)
                .orElse(producto.getPrecio());
    }

    @Transactional(readOnly = true)
    public void verificarDisponibilidad(Long id, int cantidad) {
        Producto producto = obtenerEntidad(id);
        if (!producto.isActivo() || producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
        }
    }

    @Transactional
    public void descontarStock(Long id, int cantidad) {
        Producto producto = obtenerEntidad(id);
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
        }
        producto.cambiarStock(producto.getStock() - cantidad);
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private ProductoResponse aResponse(Producto producto) {
        Instant ahora = Instant.now();
        BigDecimal precioOferta = ofertaRepository
                .findFirstByProductoIdAndDesdeLessThanEqualAndHastaAfterOrderByDesdeDesc(
                        producto.getId(), ahora, ahora)
                .map(Oferta::getPrecioOferta)
                .orElse(null);
        BigDecimal precioFinal = precioOferta != null ? precioOferta : producto.getPrecio();
        String imagenUrl = productoImagenRepository
                .findFirstByProductoIdOrderByOrdenAsc(producto.getId())
                .map(ProductoImagen::getUrl)
                .orElse(null);

        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                precioOferta,
                precioFinal,
                producto.getStock(),
                producto.getStockCritico(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre(),
                imagenUrl,
                producto.isActivo());
    }
}
