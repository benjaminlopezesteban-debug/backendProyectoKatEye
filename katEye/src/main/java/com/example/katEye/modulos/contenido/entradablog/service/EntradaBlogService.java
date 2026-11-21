package com.example.katEye.modulos.contenido.entradablog.service;

import com.example.katEye.modulos.contenido.entradablog.dto.BloqueBlog;
import com.example.katEye.modulos.contenido.entradablog.dto.EntradaBlogRequest;
import com.example.katEye.modulos.contenido.entradablog.dto.EntradaBlogResponse;
import com.example.katEye.modulos.contenido.entradablog.dto.ItemLista;
import com.example.katEye.modulos.contenido.entradablog.entity.EntradaBlog;
import com.example.katEye.modulos.contenido.entradablog.exception.EntradaNoEncontradaException;
import com.example.katEye.modulos.contenido.entradablog.exception.SlugEntradaDuplicadoException;
import com.example.katEye.modulos.contenido.entradablog.repository.EntradaBlogRepository;
import com.example.katEye.shared.exception.SolicitudInvalidaException;
import com.example.katEye.shared.security.SecurityUtils;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class EntradaBlogService {

    private static final TypeReference<List<BloqueBlog>> TIPO_CONTENIDO = new TypeReference<>() {
    };
    private static final int SLUG_MAX = 100;

    private final EntradaBlogRepository entradaBlogRepository;
    private final ObjectMapper objectMapper;

    public EntradaBlogService(
            EntradaBlogRepository entradaBlogRepository,
            ObjectMapper objectMapper) {
        this.entradaBlogRepository = entradaBlogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<EntradaBlogResponse> listar(Boolean publicada) {
        List<EntradaBlog> entradas;
        if (!SecurityUtils.esAdministrador()) {
            entradas = entradaBlogRepository.findAllByPublicadaTrueOrderByPublicadaEnDescIdDesc();
        } else if (publicada == null) {
            entradas = entradaBlogRepository.findAllByOrderByIdDesc();
        } else {
            entradas = entradaBlogRepository.findAllByPublicadaOrderByIdDesc(publicada);
        }
        return entradas.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public EntradaBlogResponse obtenerPorSlug(String slug) {
        EntradaBlog entrada = entradaBlogRepository.findBySlugIgnoreCase(slug.trim())
                .orElseThrow(() -> new EntradaNoEncontradaException(slug));
        if (!entrada.isPublicada() && !SecurityUtils.esAdministrador()) {
            throw new EntradaNoEncontradaException(slug);
        }
        return aResponse(entrada);
    }

    @Transactional
    public EntradaBlogResponse crear(EntradaBlogRequest request) {
        Long autorId = SecurityUtils.usuarioActualIdRequerido();
        String slug = normalizarSlug(resolverSlug(request.slug(), request.titulo()));
        if (entradaBlogRepository.existsBySlugIgnoreCase(slug)) {
            throw new SlugEntradaDuplicadoException(slug);
        }
        List<BloqueBlog> contenido = normalizarContenido(request.contenido());
        EntradaBlog entrada = new EntradaBlog(
                autorId,
                slug,
                request.titulo().trim(),
                recortar(request.resumen()),
                recortar(request.imagen()),
                objectMapper.valueToTree(contenido));
        return aResponse(entradaBlogRepository.save(entrada));
    }

    @Transactional
    public EntradaBlogResponse actualizar(Long id, EntradaBlogRequest request) {
        EntradaBlog entrada = obtenerEntidad(id);
        String slug = normalizarSlug(resolverSlug(request.slug(), request.titulo()));
        if (entradaBlogRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new SlugEntradaDuplicadoException(slug);
        }
        List<BloqueBlog> contenido = normalizarContenido(request.contenido());
        entrada.actualizar(
                slug,
                request.titulo().trim(),
                recortar(request.resumen()),
                recortar(request.imagen()),
                objectMapper.valueToTree(contenido));
        return aResponse(entrada);
    }

    @Transactional
    public EntradaBlogResponse cambiarPublicacion(Long id, boolean publicada) {
        EntradaBlog entrada = obtenerEntidad(id);
        if (publicada && !entrada.isPublicada()) {
            entrada.publicar(Instant.now());
        } else if (!publicada) {
            entrada.despublicar();
        }
        return aResponse(entrada);
    }

    private EntradaBlog obtenerEntidad(Long id) {
        return entradaBlogRepository.findById(id)
                .orElseThrow(() -> new EntradaNoEncontradaException(String.valueOf(id)));
    }

    private String resolverSlug(String slug, String titulo) {
        return slug == null || slug.isBlank() ? titulo : slug;
    }

    private String normalizarSlug(String valor) {
        String base = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isBlank()) {
            throw new SolicitudInvalidaException("No se pudo generar un slug valido");
        }
        if (base.length() > SLUG_MAX) {
            base = base.substring(0, SLUG_MAX).replaceAll("-+$", "");
        }
        return base;
    }

    private List<BloqueBlog> normalizarContenido(List<BloqueBlog> contenido) {
        List<BloqueBlog> normalizado = new ArrayList<>();
        for (BloqueBlog bloque : contenido) {
            String tipo = bloque.tipo().trim().toLowerCase(Locale.ROOT);
            if (tipo.equals("lista")) {
                if (bloque.items() == null || bloque.items().isEmpty()) {
                    throw new SolicitudInvalidaException("El bloque lista requiere items");
                }
                List<ItemLista> items = bloque.items().stream()
                        .map(item -> new ItemLista(item.etiqueta().trim(), item.texto().trim()))
                        .toList();
                normalizado.add(new BloqueBlog(tipo, null, items));
            } else if (tipo.equals("titulo") || tipo.equals("parrafo")) {
                if (bloque.texto() == null || bloque.texto().isBlank()) {
                    throw new SolicitudInvalidaException(
                            "El bloque " + tipo + " requiere texto");
                }
                normalizado.add(new BloqueBlog(tipo, bloque.texto().trim(), null));
            } else {
                throw new SolicitudInvalidaException("Tipo de bloque invalido: " + bloque.tipo());
            }
        }
        return normalizado;
    }

    private String recortar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private EntradaBlogResponse aResponse(EntradaBlog entrada) {
        List<BloqueBlog> contenido = entrada.getBloques() == null || entrada.getBloques().isNull()
                ? List.of()
                : objectMapper.convertValue(entrada.getBloques(), TIPO_CONTENIDO);
        return new EntradaBlogResponse(
                entrada.getId(),
                entrada.getSlug(),
                entrada.getTitulo(),
                entrada.getResumen(),
                entrada.getImagenUrl(),
                contenido,
                entrada.isPublicada(),
                entrada.getPublicadaEn(),
                entrada.getAutorUsuarioId());
    }
}
