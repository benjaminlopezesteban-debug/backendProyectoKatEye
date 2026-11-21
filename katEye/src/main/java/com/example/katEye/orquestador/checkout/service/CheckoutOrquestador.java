package com.example.katEye.orquestador.checkout.service;

import com.example.katEye.modulos.carrito.carrito.dto.ItemCarritoRequest;
import com.example.katEye.modulos.carrito.carrito.service.CarritoService;
import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import com.example.katEye.modulos.pedidos.pedido.dto.LineaPedidoCommand;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoCommand;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoResponse;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.modulos.ubicacion.comuna.service.ComunaService;
import com.example.katEye.modulos.usuarios.usuario.entity.Usuario;
import com.example.katEye.modulos.usuarios.usuario.service.UsuarioService;
import com.example.katEye.orquestador.checkout.dto.CheckoutRequest;
import com.example.katEye.orquestador.checkout.dto.CheckoutResponse;
import com.example.katEye.orquestador.checkout.dto.ItemCheckoutRequest;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.exception.SolicitudInvalidaException;
import com.example.katEye.shared.idempotency.IdempotenciaService;
import com.example.katEye.shared.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutOrquestador {

    private static final String ENDPOINT = "checkout";
    private static final int CANTIDAD_MAXIMA = 10;

    private final IdempotenciaService idempotenciaService;
    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final ComunaService comunaService;
    private final PedidoService pedidoService;
    private final MovimientoStockService movimientoStockService;

    public CheckoutOrquestador(
            IdempotenciaService idempotenciaService,
            CarritoService carritoService,
            ProductoService productoService,
            UsuarioService usuarioService,
            ComunaService comunaService,
            PedidoService pedidoService,
            MovimientoStockService movimientoStockService) {
        this.idempotenciaService = idempotenciaService;
        this.carritoService = carritoService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.comunaService = comunaService;
        this.pedidoService = pedidoService;
        this.movimientoStockService = movimientoStockService;
    }

    @Transactional
    public CheckoutResponse confirmar(String clave, CheckoutRequest request) {
        Optional<Long> previo = idempotenciaService.buscar(clave, ENDPOINT);
        if (previo.isPresent()) {
            PedidoResponse pedido = pedidoService.obtenerResponsePorId(previo.get());
            return new CheckoutResponse(
                    pedido.id(), pedido.numero(), pedido.total(), pedido.estado());
        }

        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        Usuario usuario = usuarioId == null ? null : usuarioService.obtenerEntidad(usuarioId);
        List<ItemCheckoutRequest> items = resolverItems(request, usuarioId);
        Map<Long, Integer> cantidades = consolidar(items);

        List<LineaPedidoCommand> lineas = new ArrayList<>();
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Long productoId = entrada.getKey();
            int cantidad = entrada.getValue();
            productoService.verificarDisponibilidad(productoId, cantidad);
            Producto producto = productoService.obtenerEntidad(productoId);
            BigDecimal precioUnitario = productoService.obtenerPrecioVigente(productoId);
            lineas.add(new LineaPedidoCommand(
                    productoId,
                    producto.getNombre(),
                    producto.getPrecio(),
                    precioUnitario,
                    cantidad));
        }

        String tipoEntrega = request.tipoEntrega().trim().toUpperCase();
        boolean despacho = "DESPACHO".equals(tipoEntrega);
        String direccion = despacho ? recortar(request.direccion()) : null;
        String regionCodigo = despacho ? recortar(request.regionCodigo()) : null;
        String comunaCodigo = despacho ? recortar(request.comunaCodigo()) : null;
        if (despacho && regionCodigo != null && comunaCodigo != null) {
            comunaService.validarPertenenciaRegion(comunaCodigo, regionCodigo);
        }

        PedidoResponse pedido = pedidoService.crear(new PedidoCommand(
                usuarioId,
                tipoEntrega,
                elegir(request.contactoNombre(), usuario == null ? null : usuario.getNombre()),
                elegir(request.contactoCorreo(), usuario == null ? null : usuario.getCorreo()),
                elegir(request.contactoTelefono(), usuario == null ? null : usuario.getTelefono()),
                direccion,
                regionCodigo,
                comunaCodigo,
                request.metodoPago().trim().toUpperCase(),
                clave,
                lineas));

        for (LineaPedidoCommand linea : lineas) {
            movimientoStockService.reservar(
                    linea.productoId(), linea.cantidad(), pedido.id());
        }

        if (request.carritoId() != null) {
            carritoService.vaciar(request.carritoId());
        } else if (usuarioId != null && (request.items() == null || request.items().isEmpty())) {
            carritoService.vaciar(carritoService.obtenerOCrear().id());
        }

        idempotenciaService.registrar(clave, ENDPOINT, pedido.id());
        return new CheckoutResponse(
                pedido.id(), pedido.numero(), pedido.total(), pedido.estado());
    }

    private List<ItemCheckoutRequest> resolverItems(CheckoutRequest request, Long usuarioId) {
        if (request.items() != null && !request.items().isEmpty()) {
            return request.items();
        }
        if (request.carritoId() != null) {
            return aItems(carritoService.itemsDe(request.carritoId()));
        }
        if (usuarioId != null) {
            return aItems(carritoService.itemsMios());
        }
        throw new SolicitudInvalidaException(
                "El checkout requiere items o un carrito con productos");
    }

    private List<ItemCheckoutRequest> aItems(List<ItemCarritoRequest> items) {
        if (items.isEmpty()) {
            throw new SolicitudInvalidaException("El carrito no tiene productos");
        }
        return items.stream()
                .map(item -> new ItemCheckoutRequest(item.productoId(), item.cantidad()))
                .toList();
    }

    private Map<Long, Integer> consolidar(List<ItemCheckoutRequest> items) {
        Map<Long, Integer> cantidades = new TreeMap<>();
        for (ItemCheckoutRequest item : items) {
            int cantidad = cantidades.getOrDefault(item.productoId(), 0) + item.cantidad();
            if (cantidad > CANTIDAD_MAXIMA) {
                throw new ReglaNegocioException(
                        "La cantidad maxima por producto es " + CANTIDAD_MAXIMA);
            }
            cantidades.put(item.productoId(), cantidad);
        }
        return cantidades;
    }

    private String elegir(String valor, String respaldo) {
        return valor == null || valor.isBlank() ? respaldo : valor.trim();
    }

    private String recortar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
