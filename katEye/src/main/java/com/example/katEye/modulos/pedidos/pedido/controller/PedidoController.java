package com.example.katEye.modulos.pedidos.pedido.controller;

import com.example.katEye.modulos.pedidos.historial.dto.HistorialEstadoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.CambiarEstadoRequest;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoProductoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.PedidoResponse;
import com.example.katEye.modulos.pedidos.pedido.dto.SeguimientoRequest;
import com.example.katEye.modulos.pedidos.pedido.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public List<PedidoResponse> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/mios")
    public List<PedidoResponse> listarMios() {
        return pedidoService.listarMios();
    }

    @GetMapping("/{numero}")
    public PedidoResponse obtener(@PathVariable String numero) {
        return pedidoService.obtener(numero);
    }

    @GetMapping("/{numero}/invitado")
    public PedidoResponse obtenerInvitado(
            @PathVariable String numero,
            @RequestParam(required = false) String correo) {
        return pedidoService.obtenerInvitado(numero, correo);
    }

    @PatchMapping("/{numero}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public PedidoResponse cambiarEstado(
            @PathVariable String numero,
            @Valid @RequestBody CambiarEstadoRequest request) {
        return pedidoService.cambiarEstado(numero, request.estado(), request.motivo());
    }

    @PatchMapping("/{numero}/cancelar")
    public PedidoResponse cancelar(@PathVariable String numero) {
        return pedidoService.cancelar(numero, "Pedido cancelado");
    }

    @PatchMapping("/{numero}/seguimiento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    public PedidoResponse registrarSeguimiento(
            @PathVariable String numero,
            @Valid @RequestBody SeguimientoRequest request) {
        return pedidoService.registrarSeguimiento(
                numero, request.transportista(), request.codigoSeguimiento());
    }

    @GetMapping("/{id}/productos")
    public List<PedidoProductoResponse> listarLineas(@PathVariable Long id) {
        return pedidoService.listarLineas(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialEstadoResponse> listarHistorial(@PathVariable Long id) {
        return pedidoService.listarHistorial(id);
    }
}
