package com.example.katEye.modulos.pagos.pago.controller;

import com.example.katEye.modulos.pagos.pago.dto.PagoRequest;
import com.example.katEye.modulos.pagos.pago.dto.PagoResponse;
import com.example.katEye.modulos.pagos.pago.service.PagoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/simular")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse simular(
            @RequestHeader("Idempotency-Key") String clave,
            @Valid @RequestBody PagoRequest request) {
        return pagoService.simular(clave, request);
    }

    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }

    @GetMapping("/pedido/{numero}")
    public List<PagoResponse> listarPorPedido(@PathVariable String numero) {
        return pagoService.listarPorPedido(numero);
    }

    @PostMapping("/{numero}/reintentar")
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse reintentar(
            @PathVariable String numero,
            @RequestParam(required = false) String correo,
            @RequestHeader("Idempotency-Key") String clave) {
        return pagoService.reintentar(numero, correo, clave);
    }
}
