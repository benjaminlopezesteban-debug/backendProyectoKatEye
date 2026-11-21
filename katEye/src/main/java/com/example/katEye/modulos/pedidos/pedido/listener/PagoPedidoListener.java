package com.example.katEye.modulos.pedidos.pedido.listener;

import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import com.example.katEye.shared.event.PagoAprobadoEvent;
import com.example.katEye.shared.event.PagoRechazadoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PagoPedidoListener {

    private final PedidoService pedidoService;

    public PagoPedidoListener(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @EventListener
    public void pagoAprobado(PagoAprobadoEvent evento) {
        pedidoService.marcarPagado(evento.pedidoId(), evento.pagoId());
    }

    @EventListener
    public void pagoRechazado(PagoRechazadoEvent evento) {
        pedidoService.marcarPagoRechazado(evento.pedidoId(), evento.motivo());
    }
}
