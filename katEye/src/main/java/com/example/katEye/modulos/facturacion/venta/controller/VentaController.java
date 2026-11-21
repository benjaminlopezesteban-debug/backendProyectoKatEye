package com.example.katEye.modulos.facturacion.venta.controller;

import com.example.katEye.modulos.facturacion.venta.dto.VentaResponse;
import com.example.katEye.modulos.facturacion.venta.service.VentaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public List<VentaResponse> listar() {
        return ventaService.listar();
    }

    @GetMapping("/{id}")
    public VentaResponse obtener(
            @PathVariable Long id,
            @RequestParam(required = false) String correo) {
        return ventaService.obtener(id, correo);
    }

    @GetMapping("/pedido/{pedidoId}")
    public VentaResponse obtenerPorPedido(
            @PathVariable Long pedidoId,
            @RequestParam(required = false) String correo) {
        return ventaService.obtenerPorPedido(pedidoId, correo);
    }

    @PatchMapping("/{id}/anular")
    public VentaResponse anular(@PathVariable Long id) {
        return ventaService.anular(id);
    }
}
