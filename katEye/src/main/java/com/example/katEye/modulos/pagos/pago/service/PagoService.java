package com.example.katEye.modulos.pagos.pago.service;

import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import com.example.katEye.modulos.pagos.pago.dto.PagoRequest;
import com.example.katEye.modulos.pagos.pago.dto.PagoResponse;
import com.example.katEye.modulos.pagos.pago.entity.EstadoPago;
import com.example.katEye.modulos.pagos.pago.entity.TransaccionPago;
import com.example.katEye.modulos.pagos.pago.exception.PagoNoEncontradoException;
import com.example.katEye.modulos.pagos.pago.exception.PedidoNoPagableException;
import com.example.katEye.modulos.pagos.pago.repository.PagoRepository;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoPagoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoProductoResponse;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.shared.event.ItemEvento;
import com.example.katEye.shared.event.PagoAprobadoEvent;
import com.example.katEye.shared.event.PagoRechazadoEvent;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.idempotency.IdempotenciaService;
import com.example.katEye.shared.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private static final String ENDPOINT_SIMULAR = "pagos/simular";
    private static final String ENDPOINT_REINTENTAR = "pagos/reintentar";

    private final PagoRepository pagoRepository;
    private final PedidoService pedidoService;
    private final MovimientoStockService movimientoStockService;
    private final IdempotenciaService idempotenciaService;
    private final ApplicationEventPublisher eventPublisher;
    private final String resultadoSimulado;

    public PagoService(
            PagoRepository pagoRepository,
            PedidoService pedidoService,
            MovimientoStockService movimientoStockService,
            IdempotenciaService idempotenciaService,
            ApplicationEventPublisher eventPublisher,
            @Value("${kateye.pago.simulado-resultado:APROBADA}") String resultadoSimulado) {
        this.pagoRepository = pagoRepository;
        this.pedidoService = pedidoService;
        this.movimientoStockService = movimientoStockService;
        this.idempotenciaService = idempotenciaService;
        this.eventPublisher = eventPublisher;
        this.resultadoSimulado = resultadoSimulado;
    }

    @Transactional
    public PagoResponse simular(String clave, PagoRequest request) {
        Optional<Long> previo = idempotenciaService.buscar(clave, ENDPOINT_SIMULAR);
        if (previo.isPresent()) {
            return aResponse(obtenerEntidad(previo.get()));
        }
        PedidoPagoResponse pedido = pedidoService.obtenerParaPago(request.pedidoId());
        if (pedido.usuarioId() != null) {
            validarAcceso(pedido, null);
        }
        if (!"PENDIENTE_PAGO".equals(pedido.estado())) {
            throw new PedidoNoPagableException(pedido.estado());
        }
        if (!pedido.metodoPago().equalsIgnoreCase(request.metodo())) {
            throw new ReglaNegocioException(
                    "El metodo de pago no coincide con el registrado en el pedido");
        }
        return ejecutar(pedido, clave, ENDPOINT_SIMULAR);
    }

    @Transactional
    public PagoResponse reintentar(String numero, String correo, String clave) {
        String claveFinal = clave == null || clave.isBlank()
                ? "reintento-" + UUID.randomUUID()
                : clave;
        Optional<Long> previo = idempotenciaService.buscar(claveFinal, ENDPOINT_REINTENTAR);
        if (previo.isPresent()) {
            return aResponse(obtenerEntidad(previo.get()));
        }
        PedidoPagoResponse pedido = pedidoService.obtenerParaPagoPorNumero(numero);
        validarAcceso(pedido, correo);
        if ("PAGO_RECHAZADO".equals(pedido.estado())) {
            for (PedidoProductoResponse item : pedido.items()) {
                movimientoStockService.reservar(
                        item.productoId(), item.cantidad(), pedido.id());
            }
            pedidoService.reabrirParaPago(pedido.id());
        } else if (!"PENDIENTE_PAGO".equals(pedido.estado())) {
            throw new PedidoNoPagableException(pedido.estado());
        }
        return ejecutar(pedido, claveFinal, ENDPOINT_REINTENTAR);
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        TransaccionPago pago = obtenerEntidad(id);
        PedidoPagoResponse pedido = pedidoService.obtenerParaPago(pago.getPedidoId());
        validarAcceso(pedido, null);
        return aResponse(pago);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorPedido(String numero) {
        PedidoPagoResponse pedido = pedidoService.obtenerParaPagoPorNumero(numero);
        validarAcceso(pedido, null);
        return pagoRepository.findAllByPedidoIdOrderByIdDesc(pedido.id()).stream()
                .map(this::aResponse)
                .toList();
    }

    private PagoResponse ejecutar(PedidoPagoResponse pedido, String clave, String endpoint) {
        boolean aprobada = "APROBADA".equalsIgnoreCase(resultadoSimulado.trim());
        EstadoPago estado = aprobada ? EstadoPago.APROBADA : EstadoPago.RECHAZADA;
        String motivo = aprobada ? null : "Pago rechazado por simulacion";
        TransaccionPago pago = new TransaccionPago(
                pedido.id(),
                "SIMULADO",
                "SIM-" + UUID.randomUUID(),
                pedido.total(),
                estado,
                aprobada ? "00" : "05",
                motivo,
                clave);
        pagoRepository.save(pago);

        List<ItemEvento> items = pedido.items().stream()
                .map(item -> new ItemEvento(item.productoId(), item.cantidad()))
                .toList();
        if (aprobada) {
            eventPublisher.publishEvent(
                    new PagoAprobadoEvent(pedido.id(), pago.getId(), items));
        } else {
            eventPublisher.publishEvent(
                    new PagoRechazadoEvent(pedido.id(), motivo, items));
        }

        idempotenciaService.registrar(clave, endpoint, pago.getId());
        return aResponse(pago, pedido.numero());
    }

    private TransaccionPago obtenerEntidad(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNoEncontradoException(id));
    }

    private void validarAcceso(PedidoPagoResponse pedido, String correo) {
        if (SecurityUtils.esStaff()) {
            return;
        }
        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        if (usuarioId != null && usuarioId.equals(pedido.usuarioId())) {
            return;
        }
        if (usuarioId == null
                && pedido.usuarioId() == null
                && correo != null
                && correo.equalsIgnoreCase(pedido.contactoCorreo())) {
            return;
        }
        throw new AccessDeniedException("No puede acceder al pago de este pedido");
    }

    private PagoResponse aResponse(TransaccionPago pago) {
        String numero = pedidoService.obtenerParaPago(pago.getPedidoId()).numero();
        return aResponse(pago, numero);
    }

    private PagoResponse aResponse(TransaccionPago pago, String numeroPedido) {
        return new PagoResponse(
                pago.getId(),
                pago.getPedidoId(),
                numeroPedido,
                pago.getMonto(),
                pago.getEstado().name(),
                pago.getCodigoRespuesta(),
                pago.getMotivo(),
                pago.getReferenciaExterna());
    }
}
