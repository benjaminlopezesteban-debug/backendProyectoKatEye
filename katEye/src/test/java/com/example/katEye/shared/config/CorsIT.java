package com.example.katEye.shared.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;

class CorsIT {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CatalogoFalsoController())
            .addFilters(new CorsFilter(new CorsConfig().corsConfigurationSource("http://localhost:5173")))
            .build();

    @Test
    void permiteElPreflightDelOrigenConfigurado() throws Exception {
        mockMvc.perform(options("/api/v1/productos")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void rechazaElPreflightDeUnOrigenDesconocido() throws Exception {
        mockMvc.perform(options("/api/v1/productos")
                        .header(HttpHeaders.ORIGIN, "http://origen-no-permitido.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class CatalogoFalsoController {

        @GetMapping("/api/v1/productos")
        List<Object> listar() {
            return List.of();
        }
    }
}
