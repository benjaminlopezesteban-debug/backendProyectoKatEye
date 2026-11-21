package com.example.katEye.modulos.contenido.entradablog.controller;

import com.example.katEye.modulos.contenido.entradablog.dto.EntradaBlogRequest;
import com.example.katEye.modulos.contenido.entradablog.dto.EntradaBlogResponse;
import com.example.katEye.modulos.contenido.entradablog.dto.PublicacionRequest;
import com.example.katEye.modulos.contenido.entradablog.service.EntradaBlogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/blogs")
public class EntradaBlogController {

    private final EntradaBlogService entradaBlogService;

    public EntradaBlogController(EntradaBlogService entradaBlogService) {
        this.entradaBlogService = entradaBlogService;
    }

    @GetMapping
    public List<EntradaBlogResponse> listar(
            @RequestParam(required = false) Boolean publicada) {
        return entradaBlogService.listar(publicada);
    }

    @GetMapping("/{slug}")
    public EntradaBlogResponse obtenerPorSlug(@PathVariable String slug) {
        return entradaBlogService.obtenerPorSlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EntradaBlogResponse crear(@Valid @RequestBody EntradaBlogRequest request) {
        return entradaBlogService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EntradaBlogResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EntradaBlogRequest request) {
        return entradaBlogService.actualizar(id, request);
    }

    @PatchMapping("/{id}/publicacion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EntradaBlogResponse cambiarPublicacion(
            @PathVariable Long id,
            @Valid @RequestBody PublicacionRequest request) {
        return entradaBlogService.cambiarPublicacion(id, request.publicada());
    }
}
