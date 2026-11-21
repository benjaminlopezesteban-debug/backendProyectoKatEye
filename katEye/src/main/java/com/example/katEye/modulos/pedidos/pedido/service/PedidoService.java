package com.example.katEye.modulos.pedidos.pedido.service;

import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import com.example.katEye.modulos.pedidos.historial.dto.HistorialEstadoResponse;
import com.example.katEye.modulos.pedidos.historial.service.HistorialEstadoService;
import com.example.katEye.modulos.pedidos.pedido.dto.LineaPedidoCommand;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoCommand;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoFacturacionResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoPagoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoProductoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoResponse;
import com.example.katEye.modulos.pedidos.pedido.entity.EstadoPedido;
import com.example.katEye.modulos.pedidos.pedido.entity.MetodoPago;
import com.example.katEye.modulos.pedidos.pedido.entity.Pedido;
import com.example.katEye.modulos.pedidos.pedido.entity.TipoEntrega;
import com.example.katEye.modulos.pedidos.pedido.exception.PedidoNoEncontradoException;
import com.example.katEye.modulos.pedidos.pedido.exception.TransicionEstadoInvalidaException;
import com.example.katEye.modulos.pedidos.pedido.repository.PedidoRepository;
import com.example.katEye.modulos.pedidos.pedidoproducto.entity.PedidoProducto;
import com.example.katEye.modulos.pedidos.pedidoproducto.repository.PedidoProductoRepository;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.exception.SolicitudInvalidaException;
import com.example.katEye.shared.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PedidoRepository pedidoRepository;
    private final PedidoProductoRepository pedidoProductoRepository;
    private final HistorialEstadoService historialEstadoService;
    private final MovimientoStockService movimientoStockService;
    private final long expiracionMinutos;
    private final ZoneId zonaHoraria;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoProductoRepository pedidoProductoRepository,
            HistorialEstadoService historialEstadoService,
            MovimientoStockService movimientoStockService,
            @Value("${kateye.pedido.expiracion-minutos:30}") long expiracionMinutos,
            @Value("${kateye.pedido.zona-horaria:America/Santiago}") String zonaHoraria) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoProductoRepository = pedidoProductoRepository;
        this.historialEstadoService = historialEstadoService;
        this.movimientoStockService = movimientoStockService;
        this.expiracionMinutos = expiracionMinutos;
        this.zonaHoraria = ZoneId.of(zonaHoraria);
    }

    @Transactional
    public PedidoResponse crear(PedidoCommand command) {
        if (command.lineas() == null || command.lineas().isEmpty()) {
            throw new SolicitudInvalidaException("El pedido debe incluir al menos un producto");
        }
        TipoEntrega tipoEntrega = parsearTipoEntrega(command.tipoEntrega());
        MetodoPago metodoPago = parsearMetodoPago(command.metodoPago());
        validarEntrega(tipoEntrega, command);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal ahorro = BigDecimal.ZERO;
        for (LineaPedidoCommand linea : command.lineas()) {
            BigDecimal cantidad = BigDecimal.valueOf(linea.cantidad());
            subtotal = subtotal.add(linea.precioLista().multiply(cantidad));
            ahorro = ahorro.add(
                    linea.precioLista().subtract(linea.precioUnitario()).multiply(cantidad));
        }
        BigDecimal costoDespacho = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(ahorro).add(costoDespacho);

        Pedido pedido = new Pedido(
                generarNumero(),
                command.usuarioId(),
                tipoEntrega,
                command.contactoNombre(),
                command.contactoCorreo(),
                command.contactoTelefono(),
                command.direccion(),
                command.regionCodigo(),
                command.comunaCodigo(),
                metodoPago,
                subtotal,
                ahorro,
                costoDespacho,
                total,
                command.idempotencyKey(),
                Instant.now().plus(expiracionMinutos, ChronoUnit.MINUTES));

        for (LineaPedidoCommand linea : command.lineas()) {
            pedido.agregarLinea(new PedidoProducto(
                    pedido,
                    linea.productoId(),
                    linea.nombreSnapshot(),
                    linea.precioUnitario(),
                    (short) linea.cantidad()));
        }
        pedidoRepository.save(pedido);

        historialEstadoService.registrar(
                pedido,
                null,
                EstadoPedido.PENDIENTE_PAGO,
                command.usuarioId(),
                "Pedido creado");
        return aResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(String numero) {
        return aResponse(obtenerEntidadVisible(numero));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerInvitado(String numero, String correo) {
        if (correo == null || correo.isBlank()) {
            throw new SolicitudInvalidaException("El correo es obligatorio para consultar el pedido");
        }
        Pedido pedido = pedidoRepository
                .findByNumeroAndContactoCorreoIgnoreCaseAndUsuarioIdIsNull(
                        numero, correo.trim())
                .orElseThrow(() -> new PedidoNoEncontradoException(numero));
        return aResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAllByOrderByIdDesc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMios() {
        Long usuarioId = SecurityUtils.usuarioActualIdRequerido();
        return pedidoRepository.findByUsuarioIdOrderByIdDesc(usuarioId).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public PedidoResponse cambiarEstado(String numero, String estado, String motivo) {
        Pedido pedido = obtenerEntidad(numero);
        return transicionar(pedido, parsearEstado(estado), motivo);
    }

    @Transactional
    public PedidoResponse cancelar(String numero, String motivo) {
        Pedido pedido = obtenerEntidad(numero);
        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        if (!SecurityUtils.esStaff()
                && (usuarioId == null || !usuarioId.equals(pedido.getUsuarioId()))) {
            throw new AccessDeniedException("El pedido pertenece a otro usuario");
        }
        return transicionar(pedido, EstadoPedido.CANCELADO, motivo);
    }

    @Transactional
    public PedidoResponse registrarSeguimiento(
            String numero, String transportista, String codigoSeguimiento) {
        Pedido pedido = obtenerEntidad(numero);
        pedido.asignarSeguimiento(transportista.trim(), codigoSeguimiento.trim());
        return aResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoProductoResponse> listarLineas(Long pedidoId) {
        obtenerEntidadVisible(pedidoId);
        return pedidoProductoRepository.findByPedidoIdOrderByIdAsc(pedidoId).stream()
                .map(this::aProductoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listarHistorial(Long pedidoId) {
        obtenerEntidadVisible(pedidoId);
        return historialEstadoService.listar(pedidoId);
    }

    @Transactional(readOnly = true)
    public Pedido obtenerEntidadPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerResponsePorId(Long id) {
        return aResponse(obtenerEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public PedidoPagoResponse obtenerParaPago(Long id) {
        return aPagoResponse(obtenerEntidadPorId(id));
    }

    @Transactional(readOnly = true)
    public PedidoPagoResponse obtenerParaPagoPorNumero(String numero) {
        return aPagoResponse(obtenerEntidad(numero));
    }

    @Transactional(readOnly = true)
    public PedidoFacturacionResponse obtenerParaFacturacion(Long id) {
        return aFacturacionResponse(obtenerEntidadPorId(id));
    }

    @Transactional
    public void marcarPagado(Long pedidoId, Long pagoId) {
        Pedido pedido = obtenerEntidadPorId(pedidoId);
        transicionar(pedido, EstadoPedido.PAGADO, "Pago aprobado #" + pagoId);
    }

    @Transactional
    public void marcarPagoRechazado(Long pedidoId, String motivo) {
        Pedido pedido = obtenerEntidadPorId(pedidoId);
        transicionar(pedido, EstadoPedido.PAGO_RECHAZADO, motivo);
    }

    @Transactional
    public void reabrirParaPago(Long pedidoId) {
        Pedido pedido = obtenerEntidadPorId(pedidoId);
        transicionar(pedido, EstadoPedido.PENDIENTE_PAGO, "Reintento de pago");
    }

    @Transactional(readOnly = true)
    public Pedido obtenerEntidad(String numero) {
        return pedidoRepository.findByNumero(numero)
                .orElseThrow(() -> new PedidoNoEncontradoException(numero));
    }

    private PedidoResponse transicionar(Pedido pedido, EstadoPedido destino, String motivo) {
        EstadoPedido actual = pedido.getEstado();
        if (actual == destino) {
            throw new ReglaNegocioException("El pedido ya se encuentra en estado " + destino);
        }
        if (!actual.puedePasarA(destino)) {
            throw new TransicionEstadoInvalidaException(actual.name(), destino.name());
        }
        if ((destino == EstadoPedido.CANCELADO || destino == EstadoPedido.EXPIRADO)
                && requiereLiberarStock(actual)) {
            for (PedidoProducto linea : pedido.getLineas()) {
                movimientoStockService.liberar(
                        linea.getProductoId(), linea.getCantidad(), pedido.getId());
            }
        }
        pedido.cambiarEstado(destino);
        historialEstadoService.registrar(
                pedido,
                actual,
                destino,
                SecurityUtils.usuarioActualId().orElse(null),
                motivo);
        return aResponse(pedido);
    }

    private boolean requiereLiberarStock(EstadoPedido estado) {
        return estado == EstadoPedido.PENDIENTE_PAGO
                || estado == EstadoPedido.PAGADO
                || estado == EstadoPedido.EN_PREPARACION
                || estado == EstadoPedido.LISTO_PARA_RETIRO;
    }

    private String generarNumero() {
        String prefijo = LocalDate.now(zonaHoraria).format(FORMATO_FECHA) + "-";
        pedidoRepository.bloquearNumero(prefijo);
        int correlativo = pedidoRepository.ultimoNumeroConPrefijo(prefijo)
                .map(ultimo -> Integer.parseInt(ultimo.substring(prefijo.length())) + 1)
                .orElse(1);
        return prefijo + String.format("%04d", correlativo);
    }

    private void validarEntrega(TipoEntrega tipoEntrega, PedidoCommand command) {
        if (command.contactoNombre() == null || command.contactoNombre().isBlank()
                || command.contactoCorreo() == null || command.contactoCorreo().isBlank()
                || command.contactoTelefono() == null || command.contactoTelefono().isBlank()) {
            throw new SolicitudInvalidaException("Los datos de contacto son obligatorios");
        }
        if (tipoEntrega == TipoEntrega.DESPACHO
                && (command.direccion() == null || command.direccion().isBlank()
                        || command.regionCodigo() == null || command.regionCodigo().isBlank()
                        || command.comunaCodigo() == null || command.comunaCodigo().isBlank())) {
            throw new SolicitudInvalidaException(
                    "El despacho requiere direccion, region y comuna");
        }
    }

    private TipoEntrega parsearTipoEntrega(String valor) {
        try {
            return TipoEntrega.valueOf(valor.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new SolicitudInvalidaException("Tipo de entrega invalido: " + valor);
        }
    }

    private MetodoPago parsearMetodoPago(String valor) {
        try {
            return MetodoPago.valueOf(valor.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new SolicitudInvalidaException("Metodo de pago invalido: " + valor);
        }
    }

    private EstadoPedido parsearEstado(String valor) {
        try {
            return EstadoPedido.valueOf(valor.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new SolicitudInvalidaException("Estado de pedido invalido: " + valor);
        }
    }

    private Pedido obtenerEntidadVisible(String numero) {
        Pedido pedido = obtenerEntidad(numero);
        if (SecurityUtils.esStaff()) {
            return pedido;
        }
        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        if (usuarioId == null || !usuarioId.equals(pedido.getUsuarioId())) {
            throw new AccessDeniedException("El pedido pertenece a otro usuario");
        }
        return pedido;
    }

    private Pedido obtenerEntidadVisible(Long id) {
        Pedido pedido = obtenerEntidadPorId(id);
        if (SecurityUtils.esStaff()) {
            return pedido;
        }
        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        if (usuarioId == null || !usuarioId.equals(pedido.getUsuarioId())) {
            throw new AccessDeniedException("El pedido pertenece a otro usuario");
        }
        return pedido;
    }

    private PedidoResponse aResponse(Pedido pedido) {
        List<PedidoProductoResponse> items = pedido.getLineas().stream()
                .map(this::aProductoResponse)
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getUsuarioId(),
                pedido.getTipoEntrega().name(),
                pedido.getContactoNombre(),
                pedido.getContactoCorreo(),
                pedido.getContactoTelefono(),
                pedido.getDireccion(),
                pedido.getRegionCodigo(),
                pedido.getComunaCodigo(),
                pedido.getMetodoPago().name(),
                pedido.getEstado().name(),
                pedido.getSubtotal(),
                pedido.getAhorro(),
                pedido.getCostoDespacho(),
                pedido.getTotal(),
                pedido.getTransportista(),
                pedido.getCodigoSeguimiento(),
                pedido.getExpiraEn(),
                items);
    }

    private PedidoProductoResponse aProductoResponse(PedidoProducto linea) {
        return new PedidoProductoResponse(
                linea.getId(),
                linea.getProductoId(),
                linea.getNombreSnapshot(),
                linea.getPrecioUnitario(),
                linea.getCantidad(),
                linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
    }

    private PedidoPagoResponse aPagoResponse(Pedido pedido) {
        return new PedidoPagoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getEstado().name(),
                pedido.getMetodoPago().name(),
                pedido.getTotal(),
                pedido.getUsuarioId(),
                pedido.getContactoCorreo(),
                pedido.getLineas().stream().map(this::aProductoResponse).toList());
    }

    private PedidoFacturacionResponse aFacturacionResponse(Pedido pedido) {
        return new PedidoFacturacionResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getUsuarioId(),
                pedido.getContactoCorreo(),
                pedido.getTotal(),
                pedido.getLineas().stream().map(this::aProductoResponse).toList());
    }
}
