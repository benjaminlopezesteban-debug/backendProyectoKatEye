package com.example.katEye.modulos.facturacion.venta.service;

import com.example.katEye.modulos.facturacion.venta.dto.VentaResponse;
import com.example.katEye.modulos.facturacion.venta.entity.EstadoVenta;
import com.example.katEye.modulos.facturacion.venta.entity.Venta;
import com.example.katEye.modulos.facturacion.venta.exception.VentaNoEncontradaException;
import com.example.katEye.modulos.facturacion.venta.repository.VentaRepository;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoFacturacionResponse;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.shared.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal TOTAL_CON_IVA = BigDecimal.valueOf(119);

    private final VentaRepository ventaRepository;
    private final PedidoService pedidoService;

    public VentaService(VentaRepository ventaRepository, PedidoService pedidoService) {
        this.ventaRepository = ventaRepository;
        this.pedidoService = pedidoService;
    }

    @Transactional
    public VentaResponse crearDesdePedido(PedidoFacturacionResponse pedido) {
        Venta existente = ventaRepository.findByPedidoId(pedido.id()).orElse(null);
        if (existente != null) {
            return aResponse(existente, pedido);
        }
        BigDecimal total = pedido.total();
        BigDecimal neto = total.multiply(CIEN)
                .divide(TOTAL_CON_IVA, 0, RoundingMode.HALF_UP);
        Venta venta = ventaRepository.save(new Venta(
                pedido.id(),
                Instant.now(),
                neto,
                total.subtract(neto),
                total,
                EstadoVenta.EMITIDA));
        return aResponse(venta, pedido);
    }

    @Transactional(readOnly = true)
    public java.util.List<VentaResponse> listar() {
        exigirStaff();
        return ventaRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha")).stream()
                .map(venta -> aResponse(
                        venta, pedidoService.obtenerParaFacturacion(venta.getPedidoId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id, String correo) {
        Venta venta = obtenerEntidad(id);
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(venta.getPedidoId());
        validarAcceso(pedido, correo);
        return aResponse(venta, pedido);
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorPedido(Long pedidoId, String correo) {
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(pedidoId);
        validarAcceso(pedido, correo);
        Venta venta = ventaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new VentaNoEncontradaException(String.valueOf(pedidoId)));
        return aResponse(venta, pedido);
    }

    @Transactional
    public VentaResponse anular(Long id) {
        exigirStaff();
        Venta venta = obtenerEntidad(id);
        venta.anular();
        return aResponse(venta, pedidoService.obtenerParaFacturacion(venta.getPedidoId()));
    }

    private Venta obtenerEntidad(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new VentaNoEncontradaException(String.valueOf(id)));
    }

    private void exigirStaff() {
        if (!SecurityUtils.esStaff()) {
            throw new AccessDeniedException("Solo el personal puede administrar ventas");
        }
    }

    private void validarAcceso(PedidoFacturacionResponse pedido, String correo) {
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
                && pedido.contactoCorreo() != null
                && correo.equalsIgnoreCase(pedido.contactoCorreo())) {
            return;
        }
        throw new AccessDeniedException("No puede acceder a la venta de este pedido");
    }

    private VentaResponse aResponse(Venta venta, PedidoFacturacionResponse pedido) {
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
}
