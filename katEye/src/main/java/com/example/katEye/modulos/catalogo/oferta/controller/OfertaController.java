package com.example.katEye.modulos.catalogo.oferta.controller;

import com.example.katEye.modulos.catalogo.oferta.dto.OfertaRequest;
import com.example.katEye.modulos.catalogo.oferta.dto.OfertaResponse;
import com.example.katEye.modulos.catalogo.oferta.service.OfertaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @GetMapping
    public List<OfertaResponse> listarVigentes() {
        return ofertaService.listarVigentes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public OfertaResponse crear(@Valid @RequestBody OfertaRequest request) {
        return ofertaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public OfertaResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OfertaRequest request) {
        return ofertaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public void eliminar(@PathVariable Long id) {
        ofertaService.eliminar(id);
    }
}
