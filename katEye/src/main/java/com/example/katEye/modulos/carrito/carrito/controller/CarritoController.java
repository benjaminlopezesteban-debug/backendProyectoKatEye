package com.example.katEye.modulos.carrito.carrito.controller;

import com.example.katEye.modulos.carrito.carrito.dto.ActualizarCantidadRequest;
import com.example.katEye.modulos.carrito.carrito.dto.CarritoItemResponse;
import com.example.katEye.modulos.carrito.carrito.dto.CarritoResponse;
import com.example.katEye.modulos.carrito.carrito.dto.CarritoValidacionResponse;
import com.example.katEye.modulos.carrito.carrito.dto.ItemCarritoRequest;
import com.example.katEye.modulos.carrito.carrito.dto.ValidarCarritoRequest;
import com.example.katEye.modulos.carrito.carrito.service.CarritoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public CarritoResponse obtenerMio() {
        return carritoService.obtenerOCrear();
    }

    @GetMapping("/mio")
    public CarritoResponse obtenerMiCarrito() {
        return carritoService.obtenerOCrear();
    }

    @GetMapping("/{id}")
    public CarritoResponse obtener(@PathVariable Long id) {
        return carritoService.obtener(id);
    }

    @PostMapping
    public CarritoResponse crear() {
        return carritoService.obtenerOCrear();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciar(@PathVariable Long id) {
        carritoService.vaciar(id);
    }

    @PostMapping("/validar")
    public CarritoValidacionResponse validar(@Valid @RequestBody ValidarCarritoRequest request) {
        return carritoService.validar(request.items());
    }

    @GetMapping("/{id}/items")
    public List<CarritoItemResponse> listarItems(@PathVariable Long id) {
        return carritoService.obtener(id).items();
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CarritoResponse agregarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemCarritoRequest request) {
        return carritoService.agregarItem(id, request);
    }

    @PutMapping("/{id}/items/{productoId}")
    public CarritoResponse actualizarCantidad(
            @PathVariable Long id,
            @PathVariable Long productoId,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        return carritoService.actualizarCantidad(id, productoId, request.cantidad());
    }

    @DeleteMapping("/{id}/items/{productoId}")
    public CarritoResponse eliminarItem(
            @PathVariable Long id,
            @PathVariable Long productoId) {
        return carritoService.eliminarItem(id, productoId);
    }
}
