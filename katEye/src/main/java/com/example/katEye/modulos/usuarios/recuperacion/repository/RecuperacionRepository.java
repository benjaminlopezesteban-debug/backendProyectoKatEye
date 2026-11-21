package com.example.katEye.modulos.usuarios.recuperacion.repository;

import com.example.katEye.modulos.usuarios.recuperacion.entity.Recuperacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecuperacionRepository extends JpaRepository<Recuperacion, Long> {

    Optional<Recuperacion> findByTokenHash(String tokenHash);

    void deleteByUsuarioId(Long usuarioId);
}
