package com.example.katEye.modulos.catalogo.categoria.service;

import com.example.katEye.modulos.catalogo.categoria.dto.CategoriaRequest;
import com.example.katEye.modulos.catalogo.categoria.dto.CategoriaResponse;
import com.example.katEye.modulos.catalogo.categoria.entity.Categoria;
import com.example.katEye.modulos.catalogo.categoria.exception.CategoriaNoEncontradaException;
import com.example.katEye.modulos.catalogo.categoria.exception.SlugCategoriaDuplicadoException;
import com.example.katEye.modulos.catalogo.categoria.repository.CategoriaRepository;
import com.example.katEye.modulos.catalogo.producto.repository.ProductoRepository;
import com.example.katEye.shared.exception.ConflictoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByOrdenAsc().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Categoria obtenerEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String slug = normalizarSlug(request.slug());
        if (categoriaRepository.existsBySlugIgnoreCase(slug)) {
            throw new SlugCategoriaDuplicadoException(slug);
        }
        Categoria categoria = new Categoria(slug, request.nombre().trim(), request.orden());
        return aResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = obtenerEntidad(id);
        String slug = normalizarSlug(request.slug());
        if (categoriaRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new SlugCategoriaDuplicadoException(slug);
        }
        categoria.actualizar(slug, request.nombre().trim(), request.orden());
        return aResponse(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerEntidad(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar una categoria con productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private String normalizarSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private CategoriaResponse aResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getSlug(),
                categoria.getNombre(),
                categoria.getOrden());
    }
}
