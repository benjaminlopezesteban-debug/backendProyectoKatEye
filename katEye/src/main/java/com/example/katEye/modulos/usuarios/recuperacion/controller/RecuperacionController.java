package com.example.katEye.modulos.usuarios.recuperacion.controller;

import com.example.katEye.modulos.usuarios.recuperacion.dto.RecuperacionRequest;
import com.example.katEye.modulos.usuarios.recuperacion.dto.RecuperacionResponse;
import com.example.katEye.modulos.usuarios.recuperacion.dto.RestablecerPasswordRequest;
import com.example.katEye.modulos.usuarios.recuperacion.dto.TokenValidacionResponse;
import com.example.katEye.modulos.usuarios.recuperacion.service.RecuperacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recuperacion")
public class RecuperacionController {

    private final RecuperacionService recuperacionService;

    public RecuperacionController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @PostMapping("/solicitar")
    public RecuperacionResponse solicitar(
            @Valid @RequestBody RecuperacionRequest request,
            HttpServletRequest httpRequest) {
        return recuperacionService.solicitar(request, httpRequest);
    }

    @GetMapping("/validar")
    public TokenValidacionResponse validar(@RequestParam String token) {
        return recuperacionService.validar(token);
    }

    @PostMapping("/restablecer")
    public Map<String, String> restablecer(@Valid @RequestBody RestablecerPasswordRequest request) {
        recuperacionService.restablecer(request);
        return Map.of("mensaje", "Contrasena actualizada");
    }
}
