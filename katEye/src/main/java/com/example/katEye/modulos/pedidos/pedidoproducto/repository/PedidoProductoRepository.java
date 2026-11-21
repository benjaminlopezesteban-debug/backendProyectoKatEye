package com.example.katEye.modulos.pedidos.pedidoproducto.repository;

import com.example.katEye.modulos.pedidos.pedidoproducto.entity.PedidoProducto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoProductoRepository extends JpaRepository<PedidoProducto, Long> {

    List<PedidoProducto> findByPedidoIdOrderByIdAsc(Long pedidoId);
}
