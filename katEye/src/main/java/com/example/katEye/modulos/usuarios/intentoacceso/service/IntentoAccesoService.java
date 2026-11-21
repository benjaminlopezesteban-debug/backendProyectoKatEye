package com.example.katEye.modulos.usuarios.intentoacceso.service;

import com.example.katEye.modulos.usuarios.intentoacceso.dto.IntentoAccesoResponse;
import com.example.katEye.modulos.usuarios.intentoacceso.entity.IntentoAcceso;
import com.example.katEye.modulos.usuarios.intentoacceso.entity.TipoIntento;
import com.example.katEye.modulos.usuarios.intentoacceso.repository.IntentoAccesoRepository;
import com.example.katEye.shared.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IntentoAccesoService {

    private static final int MAX_FALLIDOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private final IntentoAccesoRepository intentoAccesoRepository;

    public IntentoAccesoService(IntentoAccesoRepository intentoAccesoRepository) {
        this.intentoAccesoRepository = intentoAccesoRepository;
    }

    @Transactional
    public void registrar(
            TipoIntento tipo,
            String correo,
            boolean exitoso,
            Long usuarioId,
            HttpServletRequest request) {
        intentoAccesoRepository.save(
                new IntentoAcceso(usuarioId, tipo, correo, ip(request), exitoso));
    }

    @Transactional(readOnly = true)
    public void verificarLimite(String correo) {
        long fallidos = intentoAccesoRepository
                .countByCorreoIgnoreCaseAndTipoAndExitosoFalseAndFechaAfter(
                        correo, TipoIntento.LOGIN, Instant.now().minus(VENTANA));
        if (fallidos >= MAX_FALLIDOS) {
            throw new ReglaNegocioException(
                    "Demasiados intentos fallidos. Intenta nuevamente en 15 minutos");
        }
    }

    @Transactional(readOnly = true)
    public List<IntentoAccesoResponse> listar() {
        return intentoAccesoRepository.findAllByOrderByFechaDesc().stream()
                .map(this::aResponse)
                .toList();
    }

    private String ip(HttpServletRequest request) {
        String reenviada = request.getHeader("X-Forwarded-For");
        if (reenviada != null && !reenviada.isBlank()) {
            return reenviada.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private IntentoAccesoResponse aResponse(IntentoAcceso intento) {
        return new IntentoAccesoResponse(
                intento.getId(),
                intento.getUsuarioId(),
                intento.getTipo().name(),
                intento.getCorreo(),
                intento.getIp(),
                intento.isExitoso(),
                intento.getFecha());
    }
}
