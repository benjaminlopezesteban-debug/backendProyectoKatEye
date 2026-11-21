package com.example.katEye.modulos.ubicacion.region.repository;

import com.example.katEye.modulos.ubicacion.region.entity.Region;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, String> {

    List<Region> findAllByOrderByNombreAsc();
}
