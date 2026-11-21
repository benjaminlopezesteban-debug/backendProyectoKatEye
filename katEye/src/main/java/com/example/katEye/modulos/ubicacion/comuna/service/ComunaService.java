package com.example.katEye.modulos.ubicacion.comuna.service;

import com.example.katEye.modulos.ubicacion.comuna.dto.ComunaResponse;
import com.example.katEye.modulos.ubicacion.comuna.entity.Comuna;
import com.example.katEye.modulos.ubicacion.comuna.exception.ComunaNoEncontradaException;
import com.example.katEye.modulos.ubicacion.comuna.repository.ComunaRepository;
import com.example.katEye.modulos.ubicacion.region.service.RegionService;
import com.example.katEye.shared.exception.ReglaNegocioException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComunaService {

    private final ComunaRepository comunaRepository;
    private final RegionService regionService;

    public ComunaService(ComunaRepository comunaRepository, RegionService regionService) {
        this.comunaRepository = comunaRepository;
        this.regionService = regionService;
    }

    @Transactional(readOnly = true)
    public List<ComunaResponse> listar(String regionCodigo) {
        List<Comuna> comunas = (regionCodigo == null || regionCodigo.isBlank())
                ? comunaRepository.findAllByOrderByNombreAsc()
                : comunaRepository.findByRegionCodigoOrderByNombreAsc(regionCodigo);
        return comunas.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public ComunaResponse obtener(String codigo) {
        return aResponse(obtenerEntidad(codigo));
    }

    @Transactional(readOnly = true)
    public Comuna obtenerEntidad(String codigo) {
        return comunaRepository.findById(codigo)
                .orElseThrow(() -> new ComunaNoEncontradaException(codigo));
    }

    @Transactional(readOnly = true)
    public void validarPertenenciaRegion(String comunaCodigo, String regionCodigo) {
        regionService.obtenerEntidad(regionCodigo);
        if (!comunaRepository.existsByCodigoAndRegionCodigo(comunaCodigo, regionCodigo)) {
            throw new ReglaNegocioException("La comuna no pertenece a la region indicada");
        }
    }

    private ComunaResponse aResponse(Comuna comuna) {
        return new ComunaResponse(
                comuna.getCodigo(),
                comuna.getNombre(),
                comuna.getRegion().getCodigo(),
                comuna.getRegion().getNombre());
    }
}
