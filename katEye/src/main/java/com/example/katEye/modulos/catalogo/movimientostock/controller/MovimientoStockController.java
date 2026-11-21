package com.example.katEye.modulos.catalogo.movimientostock.controller;

import com.example.katEye.modulos.catalogo.movimientostock.dto.MovimientoStockResponse;
import com.example.katEye.modulos.catalogo.movimientostock.service.MovimientoStockService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movimientos-stock")
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public List<MovimientoStockResponse> listar() {
        return movimientoStockService.listar();
    }
}
