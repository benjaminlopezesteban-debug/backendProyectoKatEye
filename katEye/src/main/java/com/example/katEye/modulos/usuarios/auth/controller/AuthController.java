package com.example.katEye.modulos.usuarios.auth.controller;

import com.example.katEye.modulos.usuarios.auth.dto.LoginRequest;
import com.example.katEye.modulos.usuarios.auth.dto.TokenResponse;
import com.example.katEye.modulos.usuarios.auth.service.AuthService;
import com.example.katEye.modulos.usuarios.usuario.dto.RegistroRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse registrar(
            @Valid @RequestBody RegistroRequest request,
            HttpServletRequest httpRequest) {
        return authService.registrar(request, httpRequest);
    }

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("mensaje", authService.logout());
    }
}
