package com.example.katEye.modulos.contenido.entradablog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "entradas_blog")
public class EntradaBlog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "autor_usuario_id", nullable = false)
    private Long autorUsuarioId;

    @Column(name = "slug", length = 100, nullable = false, unique = true)
    private String slug;

    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;

    @Column(name = "resumen", length = 500)
    private String resumen;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bloques")
    private JsonNode bloques;

    @Column(name = "publicada", nullable = false)
    private boolean publicada;

    @Column(name = "publicada_en")
    private Instant publicadaEn;

    protected EntradaBlog() {
    }

    public EntradaBlog(
            Long autorUsuarioId,
            String slug,
            String titulo,
            String resumen,
            String imagenUrl,
            JsonNode bloques) {
        this.autorUsuarioId = autorUsuarioId;
        this.slug = slug;
        this.titulo = titulo;
        this.resumen = resumen;
        this.imagenUrl = imagenUrl;
        this.bloques = bloques;
        this.publicada = false;
    }

    public void actualizar(
            String slug,
            String titulo,
            String resumen,
            String imagenUrl,
            JsonNode bloques) {
        this.slug = slug;
        this.titulo = titulo;
        this.resumen = resumen;
        this.imagenUrl = imagenUrl;
        this.bloques = bloques;
    }

    public void publicar(Instant fecha) {
        this.publicada = true;
        this.publicadaEn = fecha;
    }

    public void despublicar() {
        this.publicada = false;
    }

    public Long getId() {
        return id;
    }

    public Long getAutorUsuarioId() {
        return autorUsuarioId;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getResumen() {
        return resumen;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public JsonNode getBloques() {
        return bloques;
    }

    public boolean isPublicada() {
        return publicada;
    }

    public Instant getPublicadaEn() {
        return publicadaEn;
    }
}
