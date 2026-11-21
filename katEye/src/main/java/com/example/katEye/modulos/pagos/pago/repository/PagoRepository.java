package com.example.katEye.modulos.pagos.pago.repository;

import com.example.katEye.modulos.pagos.pago.entity.TransaccionPago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<TransaccionPago, Long> {

    List<TransaccionPago> findAllByPedidoIdOrderByIdDesc(Long pedidoId);
}
