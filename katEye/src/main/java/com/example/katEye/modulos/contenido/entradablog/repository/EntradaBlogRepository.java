package com.example.katEye.modulos.contenido.entradablog.repository;

import com.example.katEye.modulos.contenido.entradablog.entity.EntradaBlog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaBlogRepository extends JpaRepository<EntradaBlog, Long> {

    List<EntradaBlog> findAllByPublicadaTrueOrderByPublicadaEnDescIdDesc();

    List<EntradaBlog> findAllByOrderByIdDesc();

    List<EntradaBlog> findAllByPublicadaOrderByIdDesc(boolean publicada);

    Optional<EntradaBlog> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
}
