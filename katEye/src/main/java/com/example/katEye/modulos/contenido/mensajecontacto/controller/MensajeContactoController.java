package com.example.katEye.modulos.contenido.mensajecontacto.controller;

import com.example.katEye.modulos.contenido.mensajecontacto.dto.CambiarEstadoMensajeRequest;
import com.example.katEye.modulos.contenido.mensajecontacto.dto.MensajeContactoRequest;
import com.example.katEye.modulos.contenido.mensajecontacto.dto.MensajeContactoResponse;
import com.example.katEye.modulos.contenido.mensajecontacto.entity.EstadoMensaje;
import com.example.katEye.modulos.contenido.mensajecontacto.service.MensajeContactoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contacto")
public class MensajeContactoController {

    private final MensajeContactoService mensajeContactoService;

    public MensajeContactoController(MensajeContactoService mensajeContactoService) {
        this.mensajeContactoService = mensajeContactoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensajeContactoResponse enviar(@Valid @RequestBody MensajeContactoRequest request) {
        return mensajeContactoService.enviar(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<MensajeContactoResponse> listar(
            @RequestParam(required = false) EstadoMensaje estado) {
        return mensajeContactoService.listar(estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MensajeContactoResponse obtener(@PathVariable Long id) {
        return mensajeContactoService.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MensajeContactoResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoMensajeRequest request) {
        return mensajeContactoService.cambiarEstado(
                id, EstadoMensaje.valueOf(request.estado()));
    }
}
