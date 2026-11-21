package com.example.katEye.modulos.usuarios.intentoacceso.controller;

import com.example.katEye.modulos.usuarios.intentoacceso.dto.IntentoAccesoResponse;
import com.example.katEye.modulos.usuarios.intentoacceso.service.IntentoAccesoService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/intentos-acceso")
public class IntentoAccesoController {

    private final IntentoAccesoService intentoAccesoService;

    public IntentoAccesoController(IntentoAccesoService intentoAccesoService) {
        this.intentoAccesoService = intentoAccesoService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<IntentoAccesoResponse> listar() {
        return intentoAccesoService.listar();
    }
}
