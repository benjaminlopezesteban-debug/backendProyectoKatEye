package com.example.katEye.modulos.ubicacion.region.service;

import com.example.katEye.modulos.ubicacion.region.dto.RegionResponse;
import com.example.katEye.modulos.ubicacion.region.entity.Region;
import com.example.katEye.modulos.ubicacion.region.exception.RegionNoEncontradaException;
import com.example.katEye.modulos.ubicacion.region.repository.RegionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegionService {

    private final RegionRepository regionRepository;

    public RegionService(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> listar() {
        return regionRepository.findAllByOrderByNombreAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegionResponse obtener(String codigo) {
        return aResponse(obtenerEntidad(codigo));
    }

    @Transactional(readOnly = true)
    public Region obtenerEntidad(String codigo) {
        return regionRepository.findById(codigo)
                .orElseThrow(() -> new RegionNoEncontradaException(codigo));
    }

    private RegionResponse aResponse(Region region) {
        return new RegionResponse(region.getCodigo(), region.getNombre());
    }
}
