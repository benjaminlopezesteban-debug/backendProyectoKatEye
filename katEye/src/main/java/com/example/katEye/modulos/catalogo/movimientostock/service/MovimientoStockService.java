package com.example.katEye.modulos.catalogo.movimientostock.service;

import com.example.katEye.modulos.catalogo.movimientostock.dto.MovimientoStockResponse;
import com.example.katEye.modulos.catalogo.movimientostock.entity.MovimientoStock;
import com.example.katEye.modulos.catalogo.movimientostock.entity.TipoMovimiento;
import com.example.katEye.modulos.catalogo.movimientostock.repository.MovimientoStockRepository;
import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.exception.ProductoNoEncontradoException;
import com.example.katEye.modulos.catalogo.producto.exception.StockInsuficienteException;
import com.example.katEye.modulos.catalogo.producto.repository.ProductoRepository;
import com.example.katEye.shared.exception.ReglaNegocioException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoRepository productoRepository;

    public MovimientoStockService(
            MovimientoStockRepository movimientoStockRepository,
            ProductoRepository productoRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockResponse> listar() {
        return movimientoStockRepository.findAllByOrderByIdDesc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public void registrar(
            Long productoId,
            TipoMovimiento tipo,
            int cantidad,
            String motivo,
            Long pedidoId,
            Long usuarioId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId));
        registrar(producto, tipo, cantidad, motivo, pedidoId, usuarioId);
    }

    @Transactional
    public void reservar(Long productoId, int cantidad, Long pedidoId) {
        validarCantidad(cantidad);
        Producto producto = bloquearProducto(productoId);
        if (!producto.isActivo()) {
            throw new ReglaNegocioException(
                    "El producto no esta disponible: " + producto.getNombre());
        }
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock());
        }
        producto.cambiarStock(producto.getStock() - cantidad);
        movimientoStockRepository.save(new MovimientoStock(
                producto, TipoMovimiento.RESERVA, cantidad, "Reserva de checkout", pedidoId, null));
    }

    @Transactional
    public void liberar(Long productoId, int cantidad, Long pedidoId) {
        validarCantidad(cantidad);
        Producto producto = bloquearProducto(productoId);
        producto.cambiarStock(producto.getStock() + cantidad);
        movimientoStockRepository.save(new MovimientoStock(
                producto, TipoMovimiento.LIBERACION, cantidad, "Liberacion de reserva", pedidoId, null));
    }

    @Transactional
    public void confirmar(Long productoId, int cantidad, Long pedidoId) {
        validarCantidad(cantidad);
        Producto producto = bloquearProducto(productoId);
        movimientoStockRepository.save(new MovimientoStock(
                producto, TipoMovimiento.SALIDA, cantidad, "Pago aprobado", pedidoId, null));
    }

    private Producto bloquearProducto(Long productoId) {
        return productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId));
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad del movimiento debe ser mayor a 0");
        }
    }

    @Transactional
    public void registrar(
            Producto producto,
            TipoMovimiento tipo,
            int cantidad,
            String motivo,
            Long pedidoId,
            Long usuarioId) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad del movimiento debe ser mayor a 0");
        }
        movimientoStockRepository.save(
                new MovimientoStock(producto, tipo, cantidad, motivo, pedidoId, usuarioId));
    }

    private MovimientoStockResponse aResponse(MovimientoStock movimiento) {
        return new MovimientoStockResponse(
                movimiento.getId(),
                movimiento.getProducto().getId(),
                movimiento.getProducto().getNombre(),
                movimiento.getTipo().name(),
                movimiento.getCantidad(),
                movimiento.getMotivo(),
                movimiento.getPedidoId(),
                movimiento.getUsuarioId());
    }
}
