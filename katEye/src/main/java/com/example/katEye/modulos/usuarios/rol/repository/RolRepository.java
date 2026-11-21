package com.example.katEye.modulos.usuarios.rol.repository;

import com.example.katEye.modulos.usuarios.rol.entity.RolEntidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<RolEntidad, Long> {

    Optional<RolEntidad> findByNombreIgnoreCase(String nombre);
}
