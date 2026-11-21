package com.example.katEye.orquestador.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CheckoutRequest(
        Long carritoId,
        List<@Valid ItemCheckoutRequest> items,
        @NotBlank @Size(max = 20) String tipoEntrega,
        @Size(max = 100) String contactoNombre,
        @Email @Size(max = 100) String contactoCorreo,
        @Size(max = 20) String contactoTelefono,
        @Size(max = 300) String direccion,
        @Size(max = 10) String regionCodigo,
        @Size(max = 10) String comunaCodigo,
        @NotBlank @Size(max = 20) String metodoPago) {
}
