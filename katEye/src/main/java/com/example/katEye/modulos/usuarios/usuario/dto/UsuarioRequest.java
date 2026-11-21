package com.example.katEye.modulos.usuarios.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Email @Size(max = 100) String correo,
        @NotBlank @Size(min = 4, max = 72) String password,
        @Size(max = 20) String telefono,
        @NotBlank @Size(max = 10) String regionCodigo,
        @NotBlank @Size(max = 10) String comunaCodigo,
        @NotNull Long rolId) {
}
