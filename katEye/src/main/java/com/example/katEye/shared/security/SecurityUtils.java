package com.example.katEye.shared.security;

import java.util.Optional;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Long> usuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof Long usuarioId) {
            return Optional.of(usuarioId);
        }
        return Optional.empty();
    }

    public static Long usuarioActualIdRequerido() {
        return usuarioActualId()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Se requiere autenticacion"));
    }

    public static boolean esStaff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(autoridad -> autoridad.equals("ROLE_ADMINISTRADOR")
                        || autoridad.equals("ROLE_VENDEDOR"));
    }

    public static boolean esAdministrador() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(autoridad -> autoridad.equals("ROLE_ADMINISTRADOR"));
    }
}
