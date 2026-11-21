package com.example.katEye.modulos.usuarios.rol.service;

import com.example.katEye.modulos.usuarios.rol.dto.RolResponse;
import com.example.katEye.modulos.usuarios.rol.entity.RolEntidad;
import com.example.katEye.modulos.usuarios.rol.exception.RolNoEncontradoException;
import com.example.katEye.modulos.usuarios.rol.repository.RolRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RolEntidad obtenerEntidad(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RolNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public RolEntidad obtenerPorNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RolNoEncontradoException(nombre));
    }

    private RolResponse aResponse(RolEntidad rol) {
        return new RolResponse(rol.getId(), rol.getNombre());
    }
}
