package com.example.katEye.modulos.catalogo.producto.listener;

import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import com.example.katEye.shared.event.ItemEvento;
import com.example.katEye.shared.event.PagoAprobadoEvent;
import com.example.katEye.shared.event.PagoRechazadoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StockPagoListener {

    private final MovimientoStockService movimientoStockService;

    public StockPagoListener(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @EventListener
    public void pagoAprobado(PagoAprobadoEvent evento) {
        for (ItemEvento item : evento.items()) {
            movimientoStockService.confirmar(
                    item.productoId(), item.cantidad(), evento.pedidoId());
        }
    }

    @EventListener
    public void pagoRechazado(PagoRechazadoEvent evento) {
        for (ItemEvento item : evento.items()) {
            movimientoStockService.liberar(
                    item.productoId(), item.cantidad(), evento.pedidoId());
        }
    }
}
