package com.example.katEye.modulos.contenido.entradablog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BloqueBlog(
        @NotBlank @Size(max = 20) String tipo,
        @Size(max = 5000) String texto,
        List<@Valid ItemLista> items) {
}
