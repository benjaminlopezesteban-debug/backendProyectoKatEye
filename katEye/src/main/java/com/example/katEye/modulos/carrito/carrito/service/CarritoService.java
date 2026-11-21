package com.example.katEye.modulos.carrito.carrito.service;

import com.example.katEye.modulos.carrito.carrito.dto.CarritoItemResponse;
import com.example.katEye.modulos.carrito.carrito.dto.CarritoResponse;
import com.example.katEye.modulos.carrito.carrito.dto.CarritoValidacionResponse;
import com.example.katEye.modulos.carrito.carrito.dto.ItemCarritoRequest;
import com.example.katEye.modulos.carrito.carrito.entity.Carrito;
import com.example.katEye.modulos.carrito.carrito.exception.CarritoNoEncontradoException;
import com.example.katEye.modulos.carrito.carrito.repository.CarritoRepository;
import com.example.katEye.modulos.carrito.carritoitem.entity.CarritoItem;
import com.example.katEye.modulos.carrito.carritoitem.exception.ItemCarritoNoEncontradoException;
import com.example.katEye.modulos.carrito.carritoitem.repository.CarritoItemRepository;
import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private static final int CANTIDAD_MAXIMA = 10;

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final ProductoService productoService;

    public CarritoService(
            CarritoRepository carritoRepository,
            CarritoItemRepository carritoItemRepository,
            ProductoService productoService) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.productoService = productoService;
    }

    @Transactional
    public CarritoResponse obtenerOCrear() {
        Long usuarioId = SecurityUtils.usuarioActualIdRequerido();
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuarioId)));
        return aResponse(carrito);
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtener(Long id) {
        return aResponse(obtenerEntidad(id));
    }

    @Transactional
    public CarritoResponse agregarItem(Long carritoId, ItemCarritoRequest request) {
        Carrito carrito = obtenerEntidad(carritoId);
        CarritoItem item = carritoItemRepository
                .findByCarritoIdAndProductoId(carritoId, request.productoId())
                .orElse(null);
        int cantidad = request.cantidad() + (item == null ? 0 : item.getCantidad());
        validarCantidad(cantidad);
        productoService.verificarDisponibilidad(request.productoId(), cantidad);
        if (item == null) {
            carritoItemRepository.save(
                    new CarritoItem(carrito, request.productoId(), (short) cantidad));
        } else {
            item.cambiarCantidad((short) cantidad);
        }
        return aResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(Long carritoId, Long productoId, int cantidad) {
        Carrito carrito = obtenerEntidad(carritoId);
        CarritoItem item = carritoItemRepository.findByCarritoIdAndProductoId(carritoId, productoId)
                .orElseThrow(() -> new ItemCarritoNoEncontradoException(productoId));
        validarCantidad(cantidad);
        productoService.verificarDisponibilidad(productoId, cantidad);
        item.cambiarCantidad((short) cantidad);
        return aResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long carritoId, Long productoId) {
        Carrito carrito = obtenerEntidad(carritoId);
        CarritoItem item = carritoItemRepository.findByCarritoIdAndProductoId(carritoId, productoId)
                .orElseThrow(() -> new ItemCarritoNoEncontradoException(productoId));
        carritoItemRepository.delete(item);
        return aResponse(carrito);
    }

    @Transactional
    public void vaciar(Long carritoId) {
        Carrito carrito = obtenerEntidad(carritoId);
        carritoItemRepository.deleteByCarritoId(carrito.getId());
    }

    @Transactional(readOnly = true)
    public CarritoValidacionResponse validar(List<ItemCarritoRequest> items) {
        List<CarritoItemResponse> respuestas = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal ahorro = BigDecimal.ZERO;
        for (ItemCarritoRequest item : items) {
            validarCantidad(item.cantidad());
            productoService.verificarDisponibilidad(item.productoId(), item.cantidad());
            CarritoItemResponse respuesta = aItemResponse(item.productoId(), item.cantidad());
            respuestas.add(respuesta);
            subtotal = subtotal.add(respuesta.subtotal());
            ahorro = ahorro.add(respuesta.ahorro());
        }
        return new CarritoValidacionResponse(
                respuestas, subtotal, ahorro, subtotal.subtract(ahorro));
    }

    @Transactional(readOnly = true)
    public List<ItemCarritoRequest> itemsDe(Long carritoId) {
        Carrito carrito = obtenerEntidad(carritoId);
        return carritoItemRepository.findByCarritoIdOrderByIdAsc(carrito.getId()).stream()
                .map(item -> new ItemCarritoRequest(
                        item.getProductoId(), Integer.valueOf(item.getCantidad())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemCarritoRequest> itemsMios() {
        Long usuarioId = SecurityUtils.usuarioActualIdRequerido();
        return carritoRepository.findByUsuarioId(usuarioId)
                .map(carrito -> itemsDe(carrito.getId()))
                .orElseGet(List::of);
    }

    private Carrito obtenerEntidad(Long id) {
        Carrito carrito = carritoRepository.findById(id)
                .orElseThrow(() -> new CarritoNoEncontradoException(id));
        validarDuenio(carrito);
        return carrito;
    }

    private void validarDuenio(Carrito carrito) {
        Long usuarioId = SecurityUtils.usuarioActualIdRequerido();
        if (!usuarioId.equals(carrito.getUsuarioId())) {
            throw new AccessDeniedException("El carrito pertenece a otro usuario");
        }
    }

    private void validarCantidad(int cantidad) {
        if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new ReglaNegocioException(
                    "La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
    }

    private CarritoResponse aResponse(Carrito carrito) {
        List<CarritoItemResponse> items = carritoItemRepository
                .findByCarritoIdOrderByIdAsc(carrito.getId()).stream()
                .map(item -> aItemResponse(item.getProductoId(), item.getCantidad()))
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CarritoItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ahorro = items.stream()
                .map(CarritoItemResponse::ahorro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponse(
                carrito.getId(),
                carrito.getUsuarioId(),
                items,
                subtotal,
                ahorro,
                subtotal.subtract(ahorro));
    }

    private CarritoItemResponse aItemResponse(Long productoId, int cantidad) {
        Producto producto = productoService.obtenerEntidad(productoId);
        BigDecimal precioLista = producto.getPrecio();
        BigDecimal precioUnitario = productoService.obtenerPrecioVigente(productoId);
        BigDecimal subtotal = precioLista.multiply(BigDecimal.valueOf(cantidad));
        BigDecimal ahorro = precioLista.subtract(precioUnitario)
                .multiply(BigDecimal.valueOf(cantidad));
        return new CarritoItemResponse(
                productoId,
                producto.getNombre(),
                precioLista,
                precioUnitario,
                cantidad,
                subtotal,
                ahorro,
                producto.getStock(),
                producto.isActivo() && producto.getStock() >= cantidad);
    }
}
