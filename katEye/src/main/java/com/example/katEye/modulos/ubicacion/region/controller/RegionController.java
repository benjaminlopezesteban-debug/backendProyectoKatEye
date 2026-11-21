package com.example.katEye.modulos.ubicacion.region.controller;

import com.example.katEye.modulos.ubicacion.comuna.dto.ComunaResponse;
import com.example.katEye.modulos.ubicacion.comuna.service.ComunaService;
import com.example.katEye.modulos.ubicacion.region.dto.RegionResponse;
import com.example.katEye.modulos.ubicacion.region.service.RegionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regiones")
public class RegionController {

    private final RegionService regionService;
    private final ComunaService comunaService;

    public RegionController(RegionService regionService, ComunaService comunaService) {
        this.regionService = regionService;
        this.comunaService = comunaService;
    }

    @GetMapping
    public List<RegionResponse> listar() {
        return regionService.listar();
    }

    @GetMapping("/{codigo}")
    public RegionResponse obtener(@PathVariable String codigo) {
        return regionService.obtener(codigo);
    }

    @GetMapping("/{codigo}/comunas")
    public List<ComunaResponse> listarComunas(@PathVariable String codigo) {
        return comunaService.listar(codigo);
    }
}
