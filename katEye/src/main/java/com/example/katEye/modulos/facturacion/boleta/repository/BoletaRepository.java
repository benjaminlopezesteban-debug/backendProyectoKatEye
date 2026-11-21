package com.example.katEye.modulos.facturacion.boleta.repository;

import com.example.katEye.modulos.facturacion.boleta.entity.Boleta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    Optional<Boleta> findByVentaId(Long ventaId);

    Optional<Boleta> findByFolio(String folio);
}
