package com.example.katEye.modulos.catalogo.producto.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.katEye.modulos.catalogo.producto.dto.ProductoResponse;
import com.example.katEye.modulos.catalogo.producto.service.ProductoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProductoContractTest {

    private final ProductoService productoService = Mockito.mock(ProductoService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ProductoController(productoService))
            .build();

    @Test
    void getProductosExponeElContratoConImagenUrl() throws Exception {
        ProductoResponse producto = new ProductoResponse(
                1L,
                "KAT-002",
                "Crema Hidratante Ácido Hialurónico",
                "Hidratación profunda",
                new BigDecimal("14990"),
                null,
                new BigDecimal("14990"),
                40,
                8,
                1L,
                "Skincare",
                "/img/productos/kat-002.jpg",
                true);
        when(productoService.listar(null, null, null)).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("KAT-002"))
                .andExpect(jsonPath("$[0].imagenUrl").value("/img/productos/kat-002.jpg"))
                .andExpect(jsonPath("$[0].precioFinal").value(14990))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Skincare"));
    }
}
