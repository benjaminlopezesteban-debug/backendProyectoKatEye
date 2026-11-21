package com.example.katEye.modulos.facturacion.venta.repository;

import com.example.katEye.modulos.facturacion.venta.entity.Venta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByPedidoId(Long pedidoId);
}
