package com.example.katEye.modulos.usuarios.usuario.service;

import com.example.katEye.modulos.ubicacion.comuna.dto.ComunaResponse;
import com.example.katEye.modulos.ubicacion.comuna.service.ComunaService;
import com.example.katEye.modulos.ubicacion.region.dto.RegionResponse;
import com.example.katEye.modulos.ubicacion.region.service.RegionService;
import com.example.katEye.modulos.usuarios.rol.entity.RolEntidad;
import com.example.katEye.modulos.usuarios.rol.service.RolService;
import com.example.katEye.modulos.usuarios.usuario.dto.RegistroRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioRequest;
import com.example.katEye.modulos.usuarios.usuario.dto.UsuarioResponse;
import com.example.katEye.modulos.usuarios.usuario.entity.Usuario;
import com.example.katEye.modulos.usuarios.usuario.exception.CorreoYaRegistradoException;
import com.example.katEye.modulos.usuarios.usuario.exception.UsuarioNoEncontradoException;
import com.example.katEye.modulos.usuarios.usuario.repository.UsuarioRepository;
import com.example.katEye.shared.security.SecurityUtils;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolService rolService;
    private final RegionService regionService;
    private final ComunaService comunaService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolService rolService,
            RegionService regionService,
            ComunaService comunaService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolService = rolService;
        this.regionService = regionService;
        this.comunaService = comunaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByNombreAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return aResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil() {
        return obtenerPorId(SecurityUtils.usuarioActualIdRequerido());
    }

    @Transactional(readOnly = true)
    public Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarEntidadPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim());
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        RolEntidad rol = rolService.obtenerEntidad(request.rolId());
        Usuario usuario = crearUsuario(
                request.nombre(),
                request.correo(),
                request.password(),
                request.telefono(),
                request.regionCodigo(),
                request.comunaCodigo(),
                rol);
        return aResponse(usuario);
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        RolEntidad rol = rolService.obtenerPorNombre("Cliente");
        Usuario usuario = crearUsuario(
                request.nombre(),
                request.correo(),
                request.password(),
                request.telefono(),
                request.regionCodigo(),
                request.comunaCodigo(),
                rol);
        return aResponse(usuario);
    }

    @Transactional
    public UsuarioResponse asignarRol(Long id, Long rolId) {
        Usuario usuario = obtenerEntidad(id);
        RolEntidad rol = rolService.obtenerEntidad(rolId);
        usuario.asignarRol(rol);
        return aResponse(usuario);
    }

    @Transactional
    public void cambiarPassword(Long id, String password) {
        Usuario usuario = obtenerEntidad(id);
        usuario.cambiarPassword(passwordEncoder.encode(password));
    }

    private Usuario crearUsuario(
            String nombre,
            String correo,
            String password,
            String telefono,
            String regionCodigo,
            String comunaCodigo,
            RolEntidad rol) {

        String correoLimpio = correo.trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new CorreoYaRegistradoException(correoLimpio);
        }
        comunaService.validarPertenenciaRegion(comunaCodigo, regionCodigo);

        Usuario usuario = new Usuario(
                nombre.trim(),
                correoLimpio,
                passwordEncoder.encode(password),
                telefono,
                regionCodigo,
                comunaCodigo,
                rol);
        return usuarioRepository.save(usuario);
    }

    private UsuarioResponse aResponse(Usuario usuario) {
        RegionResponse region = regionService.obtener(usuario.getRegionCodigo());
        ComunaResponse comuna = comunaService.obtener(usuario.getComunaCodigo());
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                region.codigo(),
                region.nombre(),
                comuna.codigo(),
                comuna.nombre(),
                usuario.getRol().getNombre(),
                usuario.isActivo());
    }
}
