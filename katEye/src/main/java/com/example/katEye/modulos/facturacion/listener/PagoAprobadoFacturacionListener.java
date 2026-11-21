package com.example.katEye.modulos.facturacion.listener;

import com.example.katEye.modulos.facturacion.boleta.service.BoletaService;
import com.example.katEye.modulos.facturacion.venta.dto.VentaResponse;
import com.example.katEye.modulos.facturacion.venta.service.VentaService;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoFacturacionResponse;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.shared.event.PagoAprobadoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PagoAprobadoFacturacionListener {

    private final PedidoService pedidoService;
    private final VentaService ventaService;
    private final BoletaService boletaService;

    public PagoAprobadoFacturacionListener(
            PedidoService pedidoService,
            VentaService ventaService,
            BoletaService boletaService) {
        this.pedidoService = pedidoService;
        this.ventaService = ventaService;
        this.boletaService = boletaService;
    }

    @EventListener
    public void pagoAprobado(PagoAprobadoEvent evento) {
        PedidoFacturacionResponse pedido = pedidoService.obtenerParaFacturacion(evento.pedidoId());
        VentaResponse venta = ventaService.crearDesdePedido(pedido);
        boletaService.emitir(pedido, venta, null);
    }
}
