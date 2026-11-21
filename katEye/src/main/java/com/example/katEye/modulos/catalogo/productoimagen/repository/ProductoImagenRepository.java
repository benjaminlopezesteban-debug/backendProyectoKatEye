package com.example.katEye.modulos.catalogo.productoimagen.repository;

import com.example.katEye.modulos.catalogo.productoimagen.entity.ProductoImagen;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Long> {

    List<ProductoImagen> findByProductoIdOrderByOrdenAsc(Long productoId);

    Optional<ProductoImagen> findByIdAndProductoId(Long id, Long productoId);

    boolean existsByProductoIdAndOrden(Long productoId, Short orden);
}
