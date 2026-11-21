package com.example.katEye.modulos.contenido.mensajecontacto.service;

import com.example.katEye.modulos.contenido.mensajecontacto.dto.MensajeContactoRequest;
import com.example.katEye.modulos.contenido.mensajecontacto.dto.MensajeContactoResponse;
import com.example.katEye.modulos.contenido.mensajecontacto.entity.EstadoMensaje;
import com.example.katEye.modulos.contenido.mensajecontacto.entity.MensajeContacto;
import com.example.katEye.modulos.contenido.mensajecontacto.exception.MensajeNoEncontradoException;
import com.example.katEye.modulos.contenido.mensajecontacto.repository.MensajeContactoRepository;
import com.example.katEye.shared.exception.ReglaNegocioException;
import com.example.katEye.shared.security.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MensajeContactoService {

    private final MensajeContactoRepository mensajeRepository;

    public MensajeContactoService(MensajeContactoRepository mensajeRepository) {
        this.mensajeRepository = mensajeRepository;
    }

    @Transactional
    public MensajeContactoResponse enviar(MensajeContactoRequest request) {
        MensajeContacto mensaje = new MensajeContacto(
                request.nombre().trim(),
                request.correo().trim(),
                request.comentario().trim(),
                Instant.now());
        return aResponse(mensajeRepository.save(mensaje));
    }

    @Transactional(readOnly = true)
    public List<MensajeContactoResponse> listar(EstadoMensaje estado) {
        List<MensajeContacto> mensajes = estado == null
                ? mensajeRepository.findAllByOrderByIdDesc()
                : mensajeRepository.findAllByEstadoOrderByIdDesc(estado);
        return mensajes.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public MensajeContactoResponse obtener(Long id) {
        return aResponse(obtenerEntidad(id));
    }

    @Transactional
    public MensajeContactoResponse cambiarEstado(Long id, EstadoMensaje nuevo) {
        MensajeContacto mensaje = obtenerEntidad(id);
        if (mensaje.getEstado() == nuevo) {
            return aResponse(mensaje);
        }
        if (!mensaje.getEstado().puedePasarA(nuevo)) {
            throw new ReglaNegocioException(
                    "Transicion de estado invalida: "
                            + mensaje.getEstado() + " -> " + nuevo);
        }
        Long administradorId = SecurityUtils.usuarioActualIdRequerido();
        mensaje.cambiarEstado(nuevo, administradorId);
        return aResponse(mensaje);
    }

    private MensajeContacto obtenerEntidad(Long id) {
        return mensajeRepository.findById(id)
                .orElseThrow(() -> new MensajeNoEncontradoException(id));
    }

    private MensajeContactoResponse aResponse(MensajeContacto mensaje) {
        return new MensajeContactoResponse(
                mensaje.getId(),
                mensaje.getNombre(),
                mensaje.getCorreo(),
                mensaje.getComentario(),
                mensaje.getEstado().name(),
                mensaje.getRespondidoPor(),
                mensaje.getCreadoEn());
    }
}
