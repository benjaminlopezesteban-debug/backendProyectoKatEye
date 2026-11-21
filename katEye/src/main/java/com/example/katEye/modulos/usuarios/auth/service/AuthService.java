package com.example.katEye.modulos.usuarios.auth.service;

import com.example.katEye.modulos.usuarios.auth.dto.LoginRequest;
import com.example.katEye.modulos.usuarios.auth.dto.TokenResponse;
import com.example.katEye.modulos.usuarios.auth.exception.CredencialesInvalidasException;
import com.example.katEye.modulos.usuarios.intentoacceso.entity.TipoIntento;
import com.example.katEye.modulos.usuarios.intentoacceso.service.IntentoAccesoService;
import com.example.katEye.modulos.usuarios.usuario.dto.RegistroRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioResponse;
import com.example.katEye.modulos.usuarios.usuario.entity.Usuario;
import com.example.katEye.modulos.usuarios.usuario.service.UsuarioService;
import com.example.katEye.shared.security.JwtService;
import com.example.katEye.shared.security.Rol;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioService usuarioService;
    private final IntentoAccesoService intentoAccesoService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UsuarioService usuarioService,
            IntentoAccesoService intentoAccesoService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.intentoAccesoService = intentoAccesoService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String correo = request.correo().trim();
        intentoAccesoService.verificarLimite(correo);

        Usuario usuario = usuarioService.buscarEntidadPorCorreo(correo).orElse(null);
        boolean valido = usuario != null
                && usuario.isActivo()
                && passwordEncoder.matches(request.password(), usuario.getPasswordHash());

        if (!valido) {
            intentoAccesoService.registrar(
                    TipoIntento.LOGIN,
                    correo,
                    false,
                    usuario == null ? null : usuario.getId(),
                    httpRequest);
            throw new CredencialesInvalidasException();
        }

        intentoAccesoService.registrar(TipoIntento.LOGIN, correo, true, usuario.getId(), httpRequest);
        return token(usuarioService.obtenerPorId(usuario.getId()));
    }

    public TokenResponse registrar(RegistroRequest request, HttpServletRequest httpRequest) {
        UsuarioResponse usuario = usuarioService.registrar(request);
        intentoAccesoService.registrar(
                TipoIntento.REGISTRO,
                usuario.correo(),
                true,
                usuario.id(),
                httpRequest);
        return token(usuario);
    }

    public String logout() {
        return "Sesion cerrada";
    }

    private TokenResponse token(UsuarioResponse usuario) {
        Rol rol = Rol.valueOf(usuario.rol().toUpperCase());
        String token = jwtService.generar(usuario.id(), usuario.correo(), rol);
        return new TokenResponse(token, "Bearer", jwtService.expiracionSegundos(), usuario);
    }
}
