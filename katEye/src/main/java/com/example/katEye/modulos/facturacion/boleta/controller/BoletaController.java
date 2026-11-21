package com.example.katEye.modulos.facturacion.boleta.controller;

import com.example.katEye.modulos.facturacion.boleta.dto.BoletaResponse;
import com.example.katEye.modulos.facturacion.detalleboleta.dto.DetalleBoletaResponse;
import com.example.katEye.modulos.facturacion.boleta.dto.EmitirBoletaRequest;
import com.example.katEye.modulos.facturacion.boleta.service.BoletaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/boletas")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @GetMapping
    public List<BoletaResponse> listar() {
        return boletaService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoletaResponse emitir(@Valid @RequestBody EmitirBoletaRequest request) {
        return boletaService.emitirManual(request.pedidoId(), request.rutReceptor());
    }

    @GetMapping("/{folio}")
    public BoletaResponse obtener(
            @PathVariable String folio,
            @RequestParam(required = false) String correo) {
        return boletaService.obtenerPorFolio(folio, correo);
    }

    @GetMapping("/{id}/detalle")
    public List<DetalleBoletaResponse> listarDetalle(
            @PathVariable Long id,
            @RequestParam(required = false) String correo) {
        return boletaService.listarDetalle(id, correo);
    }

    @PatchMapping("/{folio}/anular")
    public BoletaResponse anular(@PathVariable String folio) {
        return boletaService.anular(folio);
    }
}
