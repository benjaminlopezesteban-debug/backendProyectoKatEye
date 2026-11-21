package com.example.katEye.orquestador.checkout.controller;

import com.example.katEye.orquestador.checkout.dto.CheckoutRequest;
import com.example.katEye.orquestador.checkout.dto.CheckoutResponse;
import com.example.katEye.orquestador.checkout.service.CheckoutOrquestador;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutOrquestador checkoutOrquestador;

    public CheckoutController(CheckoutOrquestador checkoutOrquestador) {
        this.checkoutOrquestador = checkoutOrquestador;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse confirmar(
            @RequestHeader(value = "Idempotency-Key", required = false) String clave,
            @Valid @RequestBody CheckoutRequest request) {
        return checkoutOrquestador.confirmar(clave, request);
    }
}
