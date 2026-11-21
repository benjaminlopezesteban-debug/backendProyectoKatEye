package com.example.katEye.modulos.pedidos.historial.repository;

import com.example.katEye.modulos.pedidos.historial.entity.HistorialEstadoPedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialEstadoPedidoRepository
        extends JpaRepository<HistorialEstadoPedido, Long> {

    List<HistorialEstadoPedido> findByPedidoIdOrderByFechaAscIdAsc(Long pedidoId);
}
