package com.example.katEye.modulos.pedidos.historial.service;

import com.example.katEye.modulos.pedidos.historial.dto.HistorialEstadoResponse;
import com.example.katEye.modulos.pedidos.historial.entity.HistorialEstadoPedido;
import com.example.katEye.modulos.pedidos.historial.repository.HistorialEstadoPedidoRepository;
import com.example.katEye.modulos.pedidos.pedido.entity.EstadoPedido;
import com.example.katEye.modulos.pedidos.pedido.entity.Pedido;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialEstadoService {

    private final HistorialEstadoPedidoRepository historialRepository;

    public HistorialEstadoService(HistorialEstadoPedidoRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional
    public void registrar(
            Pedido pedido,
            EstadoPedido estadoAnterior,
            EstadoPedido estadoNuevo,
            Long actorUsuarioId,
            String motivo) {
        historialRepository.save(new HistorialEstadoPedido(
                pedido, estadoAnterior, estadoNuevo, actorUsuarioId, motivo));
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoResponse> listar(Long pedidoId) {
        return historialRepository.findByPedidoIdOrderByFechaAscIdAsc(pedidoId).stream()
                .map(this::aResponse)
                .toList();
    }

    private HistorialEstadoResponse aResponse(HistorialEstadoPedido historial) {
        return new HistorialEstadoResponse(
                historial.getEstadoAnterior() == null
                        ? null
                        : historial.getEstadoAnterior().name(),
                historial.getEstadoNuevo().name(),
                historial.getActorUsuarioId(),
                historial.getMotivo(),
                historial.getFecha());
    }
}
