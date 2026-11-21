package com.example.katEye.modulos.facturacion.boleta.service;

import com.example.katEye.modulos.facturacion.boleta.dto.BoletaResponse;
import com.example.katEye.modulos.facturacion.detalleboleta.dto.DetalleBoletaResponse;
import com.example.katEye.modulos.facturacion.boleta.entity.Boleta;
import com.example.katEye.modulos.facturacion.detalleboleta.entity.DetalleBoleta;
import com.example.katEye.modulos.facturacion.boleta.entity.EstadoBoleta;
import com.example.katEye.modulos.facturacion.boleta.entity.TipoDocumento;
import com.example.katEye.modulos.facturacion.boleta.exception.BoletaNoEncontradaException;
import com.example.katEye.modulos.facturacion.boleta.repository.BoletaRepository;
import com.example.katEye.modulos.facturacion.venta.dto.VentaResponse;
import com.example.katEye.modulos.facturacion.venta.entity.Venta;
import com.example.katEye.modulos.facturacion.venta.exception.VentaNoEncontradaException;
import com.example.katEye.modulos.facturacion.venta.repository.VentaRepository;
import com.example.katEye.modulos.facturacion.venta.service.VentaService;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoFacturacionResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoProductoResponse;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.shared.exception.ConflictoException;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final VentaRepository ventaRepository;
    private final VentaService ventaService;
    private final PedidoService pedidoService;

    public BoletaService(
            BoletaRepository boletaRepository,
            VentaRepository ventaRepository,
            VentaService ventaService,
            PedidoService pedidoService) {
        this.boletaRepository = boletaRepository;
        this.ventaRepository = ventaRepository;
        this.ventaService = ventaService;
        this.pedidoService = pedidoService;
    }

    @Transactional
    public BoletaResponse emitir(
            PedidoFacturacionResponse pedido, VentaResponse venta, String rutReceptor) {
        Boleta existente = boletaRepository.findByVentaId(venta.id()).orElse(null);
        if (existente != null) {
            return aResponse(existente);
        }
        boolean factura = rutReceptor != null && !rutReceptor.isBlank();
        Boleta boleta = new Boleta(
                venta.id(),
                (factura ? "F-" : "B-") + pedido.numero(),
                factura ? TipoDocumento.FACTURA : TipoDocumento.BOLETA,
                factura ? rutReceptor.trim() : null,
                Instant.now(),
                venta.montoNeto(),
                venta.montoIva(),
                venta.total(),
                EstadoBoleta.EMITIDA);
        for (PedidoProductoResponse item : pedido.items()) {
            boleta.agregarDetalle(new DetalleBoleta(
                    item.id(),
                    item.nombreSnapshot(),
                    (short) item.cantidad(),
                    item.precioUnitario()));
        }
        boletaRepository.save(boleta);
        return aResponse(boleta);
    }

    @Transactional
    public BoletaResponse emitirManual(Long pedidoId, String rutReceptor) {
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(pedidoId);
        String estado = pedidoService.obtenerParaPago(pedidoId).estado();
        if (!"PAGADO".equals(estado)) {
            throw new ReglaNegocioException(
                    "Solo se puede emitir boleta de un pedido pagado (estado actual: " + estado + ")");
        }
        VentaResponse venta = ventaRepository.findByPedidoId(pedidoId)
                .map(this::aVentaResponse)
                .orElseGet(() -> ventaService.crearDesdePedido(pedido));
        Boleta existente = boletaRepository.findByVentaId(venta.id()).orElse(null);
        if (existente != null
                && rutReceptor != null
                && !rutReceptor.isBlank()
                && existente.getTipoDocumento() == TipoDocumento.BOLETA) {
            throw new ConflictoException(
                    "Ya existe una boleta emitida para esta venta; no puede reemplazarse por factura");
        }
        return emitir(pedido, venta, rutReceptor);
    }

    @Transactional(readOnly = true)
    public List<BoletaResponse> listar() {
        exigirStaff();
        return boletaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaEmision")).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoletaResponse obtenerPorFolio(String folio, String correo) {
        Boleta boleta = obtenerEntidadPorFolio(folio);
        validarAcceso(boleta, correo);
        return aResponse(boleta);
    }

    @Transactional(readOnly = true)
    public List<DetalleBoletaResponse> listarDetalle(Long id, String correo) {
        Boleta boleta = obtenerEntidad(id);
        validarAcceso(boleta, correo);
        return boleta.getDetalles().stream().map(this::aDetalleResponse).toList();
    }

    @Transactional
    public BoletaResponse anular(String folio) {
        exigirStaff();
        Boleta boleta = obtenerEntidadPorFolio(folio);
        boleta.anular();
        return aResponse(boleta);
    }

    private Boleta obtenerEntidad(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new BoletaNoEncontradaException(String.valueOf(id)));
    }

    private Boleta obtenerEntidadPorFolio(String folio) {
        return boletaRepository.findByFolio(folio)
                .orElseThrow(() -> new BoletaNoEncontradaException(folio));
    }

    private void exigirStaff() {
        if (!SecurityUtils.esStaff()) {
            throw new AccessDeniedException("Solo el personal puede administrar boletas");
        }
    }

    private void validarAcceso(Boleta boleta, String correo) {
        if (SecurityUtils.esStaff()) {
            return;
        }
        PedidoFacturacionResponse pedido = pedidoDe(boleta);
        Long usuarioId = SecurityUtils.usuarioActualId().orElse(null);
        if (usuarioId != null && usuarioId.equals(pedido.usuarioId())) {
            return;
        }
        if (usuarioId == null
                && pedido.usuarioId() == null
                && correo != null
                && pedido.contactoCorreo() != null
                && correo.equalsIgnoreCase(pedido.contactoCorreo())) {
            return;
        }
        throw new AccessDeniedException("No puede acceder a la boleta de este pedido");
    }

    private PedidoFacturacionResponse pedidoDe(Boleta boleta) {
        Venta venta = ventaRepository.findById(boleta.getVentaId())
                .orElseThrow(() -> new VentaNoEncontradaException(
                        String.valueOf(boleta.getVentaId())));
        return pedidoService.obtenerParaFacturacion(venta.getPedidoId());
    }

    private VentaResponse aVentaResponse(Venta venta) {
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(venta.getPedidoId());
        return new VentaResponse(
                venta.getId(),
                venta.getPedidoId(),
                pedido.numero(),
                venta.getFecha(),
                venta.getMontoNeto(),
                venta.getMontoIva(),
                venta.getTotal(),
                venta.getEstado().name());
    }

    private DetalleBoletaResponse aDetalleResponse(DetalleBoleta detalle) {
        return new DetalleBoletaResponse(
                detalle.getPedidoProductoId(),
                detalle.getDescripcion(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario());
    }

    private BoletaResponse aResponse(Boleta boleta) {
        Venta venta = ventaRepository.findById(boleta.getVentaId())
                .orElseThrow(() -> new VentaNoEncontradaException(
                        String.valueOf(boleta.getVentaId())));
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(venta.getPedidoId());
        return new BoletaResponse(
                boleta.getId(),
                boleta.getFolio(),
                boleta.getTipoDocumento().name(),
                boleta.getRutReceptor(),
                boleta.getFechaEmision(),
                boleta.getMontoNeto(),
                boleta.getMontoIva(),
                boleta.getTotal(),
                boleta.getEstado().name(),
                boleta.getVentaId(),
                pedido.id(),
                pedido.numero(),
                boleta.getDetalles().stream().map(this::aDetalleResponse).toList());
    }
}
