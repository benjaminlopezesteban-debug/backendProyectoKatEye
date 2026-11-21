package com.example.katEye.shared.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"estado\":401,\"error\":\"Unauthorized\",\"mensaje\":\"Se requiere autenticacion\"}");
                        })
                        .accessDeniedHandler((request, response, deniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"estado\":403,\"error\":\"Forbidden\",\"mensaje\":\"Acceso denegado\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/error",
                                "/actuator/health",
                                "/api/v1/auth/**",
                                "/api/v1/recuperacion/**")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/regiones/**",
                                "/api/v1/comunas/**",
                                "/api/v1/productos/**",
                                "/api/v1/categorias/**",
                                "/api/v1/ofertas/**",
                                "/api/v1/blogs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/contacto")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/checkout")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/*/invitado")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/pagos/simular")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/pagos/*/reintentar")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/boletas/*",
                                "/api/v1/boletas/*/detalle")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
