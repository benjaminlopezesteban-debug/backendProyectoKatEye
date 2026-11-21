package com.example.katEye.modulos.usuarios.recuperacion.service;

import com.example.katEye.modulos.usuarios.intentoacceso.entity.TipoIntento;
import com.example.katEye.modulos.usuarios.intentoacceso.service.IntentoAccesoService;
import com.example.katEye.modulos.usuarios.recuperacion.dto.RecuperacionRequest;
import com.example.katEye.modulos.usuarios.recuperacion.dto.RecuperacionResponse;
import com.example.katEye.modulos.usuarios.recuperacion.dto.RestablecerPasswordRequest;
import com.example.katEye.modulos.usuarios.recuperacion.dto.TokenValidacionResponse;
import com.example.katEye.modulos.usuarios.recuperacion.entity.Recuperacion;
import com.example.katEye.modulos.usuarios.recuperacion.exception.TokenInvalidoException;
import com.example.katEye.modulos.usuarios.recuperacion.repository.RecuperacionRepository;
import com.example.katEye.modulos.usuarios.usuario.entity.Usuario;
import com.example.katEye.modulos.usuarios.usuario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecuperacionService {

    private static final Duration VIGENCIA = Duration.ofMinutes(30);

    private final RecuperacionRepository recuperacionRepository;
    private final UsuarioService usuarioService;
    private final IntentoAccesoService intentoAccesoService;

    public RecuperacionService(
            RecuperacionRepository recuperacionRepository,
            UsuarioService usuarioService,
            IntentoAccesoService intentoAccesoService) {
        this.recuperacionRepository = recuperacionRepository;
        this.usuarioService = usuarioService;
        this.intentoAccesoService = intentoAccesoService;
    }

    @Transactional
    public RecuperacionResponse solicitar(RecuperacionRequest request, HttpServletRequest httpRequest) {
        String correo = request.correo().trim();
        Usuario usuario = usuarioService.buscarEntidadPorCorreo(correo).orElse(null);

        if (usuario == null) {
            intentoAccesoService.registrar(TipoIntento.RECUPERACION, correo, false, null, httpRequest);
            return new RecuperacionResponse(
                    "Si el correo esta registrado, recibiras las instrucciones", null);
        }

        String token = generarToken();
        recuperacionRepository.deleteByUsuarioId(usuario.getId());
        recuperacionRepository.save(
                new Recuperacion(usuario.getId(), hash(token), Instant.now().plus(VIGENCIA)));
        intentoAccesoService.registrar(
                TipoIntento.RECUPERACION, correo, true, usuario.getId(), httpRequest);

        return new RecuperacionResponse(
                "Token generado para restablecer la contrasena (simula el envio por correo)", token);
    }

    @Transactional(readOnly = true)
    public TokenValidacionResponse validar(String token) {
        return recuperacionRepository.findByTokenHash(hash(token))
                .filter(Recuperacion::estaVigente)
                .map(recuperacion -> new TokenValidacionResponse(
                        true,
                        usuarioService.obtenerEntidad(recuperacion.getUsuarioId()).getCorreo()))
                .orElse(new TokenValidacionResponse(false, null));
    }

    @Transactional
    public void restablecer(RestablecerPasswordRequest request) {
        Recuperacion recuperacion = recuperacionRepository.findByTokenHash(hash(request.token()))
                .filter(Recuperacion::estaVigente)
                .orElseThrow(TokenInvalidoException::new);

        usuarioService.cambiarPassword(recuperacion.getUsuarioId(), request.password());
        recuperacionRepository.delete(recuperacion);
    }

    private String generarToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
