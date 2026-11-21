package com.example.katEye.modulos.carrito.carrito.repository;

import com.example.katEye.modulos.carrito.carrito.entity.Carrito;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
