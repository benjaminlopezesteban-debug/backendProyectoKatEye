package com.example.katEye.modulos.carrito.carritoitem.repository;

import com.example.katEye.modulos.carrito.carritoitem.entity.CarritoItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    List<CarritoItem> findByCarritoIdOrderByIdAsc(Long carritoId);

    Optional<CarritoItem> findByCarritoIdAndProductoId(Long carritoId, Long productoId);

    boolean existsByCarritoIdAndProductoId(Long carritoId, Long productoId);

    void deleteByCarritoId(Long carritoId);
}
