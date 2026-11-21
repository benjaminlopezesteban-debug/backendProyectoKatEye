package com.example.katEye.modulos.ubicacion.comuna.controller;

import com.example.katEye.modulos.ubicacion.comuna.dto.ComunaResponse;
import com.example.katEye.modulos.ubicacion.comuna.service.ComunaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comunas")
public class ComunaController {

    private final ComunaService comunaService;

    public ComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    @GetMapping
    public List<ComunaResponse> listar(@RequestParam(required = false) String regionCodigo) {
        return comunaService.listar(regionCodigo);
    }

    @GetMapping("/{codigo}")
    public ComunaResponse obtener(@PathVariable String codigo) {
        return comunaService.obtener(codigo);
    }
}
