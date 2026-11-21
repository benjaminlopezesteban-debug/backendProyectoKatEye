package com.example.katEye.modulos.contenido.entradablog.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EntradaBlogRequest(
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 100) String slug,
        @Size(max = 500) String resumen,
        @Size(max = 500) @JsonAlias("imagenUrl") String imagen,
        @NotEmpty @JsonAlias("bloques") List<@Valid BloqueBlog> contenido) {
}
