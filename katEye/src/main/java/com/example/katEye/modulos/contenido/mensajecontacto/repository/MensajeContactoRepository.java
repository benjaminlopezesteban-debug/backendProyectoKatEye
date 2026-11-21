package com.example.katEye.modulos.contenido.mensajecontacto.repository;

import com.example.katEye.modulos.contenido.mensajecontacto.entity.EstadoMensaje;
import com.example.katEye.modulos.contenido.mensajecontacto.entity.MensajeContacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    List<MensajeContacto> findAllByOrderByIdDesc();

    List<MensajeContacto> findAllByEstadoOrderByIdDesc(EstadoMensaje estado);
}
