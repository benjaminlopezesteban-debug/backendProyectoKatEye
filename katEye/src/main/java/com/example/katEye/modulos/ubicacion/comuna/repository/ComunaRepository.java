package com.example.katEye.modulos.ubicacion.comuna.repository;

import com.example.katEye.modulos.ubicacion.comuna.entity.Comuna;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunaRepository extends JpaRepository<Comuna, String> {

    List<Comuna> findAllByOrderByNombreAsc();

    List<Comuna> findByRegionCodigoOrderByNombreAsc(String regionCodigo);

    boolean existsByCodigoAndRegionCodigo(String codigo, String regionCodigo);
}
