package com.example.katEye.modulos.catalogo.producto.repository;

import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByCategoriaId(Long categoriaId);

    List<Producto> findTop8ByCategoriaIdAndIdNotAndActivoTrueOrderByNombreAsc(Long categoriaId, Long id);

    @Query("""
            SELECT p FROM Producto p
            WHERE p.activo = true
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:precioMin IS NULL OR p.precio >= :precioMin)
              AND (:precioMax IS NULL OR p.precio <= :precioMax)
            ORDER BY p.nombre
            """)
    List<Producto> buscar(
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax);

    @Query("""
            SELECT p FROM Producto p
            WHERE p.activo = true
              AND p.stockCritico IS NOT NULL
              AND p.stock <= p.stockCritico
            ORDER BY p.stock ASC
            """)
    List<Producto> listarStockCritico();
}
