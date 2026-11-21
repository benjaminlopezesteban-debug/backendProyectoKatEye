package com.example.katEye.modulos.catalogo.oferta.repository;

import com.example.katEye.modulos.catalogo.oferta.entity.Oferta;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @Query("""
            SELECT o FROM Oferta o
            WHERE o.desde <= :ahora AND o.hasta > :ahora
            ORDER BY o.desde DESC
            """)
    List<Oferta> findVigentes(@Param("ahora") Instant ahora);

    Optional<Oferta> findFirstByProductoIdAndDesdeLessThanEqualAndHastaAfterOrderByDesdeDesc(
            Long productoId, Instant desde, Instant hasta);

    List<Oferta> findByProductoIdOrderByDesdeDesc(Long productoId);
}
