package com.example.katEye.modulos.usuarios.intentoacceso.repository;

import com.example.katEye.modulos.usuarios.intentoacceso.entity.IntentoAcceso;
import com.example.katEye.modulos.usuarios.intentoacceso.entity.TipoIntento;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoAccesoRepository extends JpaRepository<IntentoAcceso, Long> {

    long countByCorreoIgnoreCaseAndTipoAndExitosoFalseAndFechaAfter(
            String correo, TipoIntento tipo, Instant desde);

    List<IntentoAcceso> findAllByOrderByFechaDesc();
}
