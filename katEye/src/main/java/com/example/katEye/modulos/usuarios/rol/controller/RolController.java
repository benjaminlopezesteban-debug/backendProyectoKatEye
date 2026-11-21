package com.example.katEye.modulos.usuarios.rol.controller;

import com.example.katEye.modulos.usuarios.rol.dto.RolResponse;
import com.example.katEye.modulos.usuarios.rol.service.RolService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<RolResponse> listar() {
        return rolService.listar();
    }
}
