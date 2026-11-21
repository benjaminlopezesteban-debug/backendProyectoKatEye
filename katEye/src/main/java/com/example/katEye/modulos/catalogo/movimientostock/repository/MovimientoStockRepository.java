package com.example.katEye.modulos.catalogo.movimientostock.repository;

import com.example.katEye.modulos.catalogo.movimientostock.entity.MovimientoStock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findAllByOrderByIdDesc();
}
