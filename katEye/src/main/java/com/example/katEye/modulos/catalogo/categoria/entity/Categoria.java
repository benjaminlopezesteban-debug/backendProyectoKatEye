package com.example.katEye.modulos.catalogo.categoria.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slug", length = 60, nullable = false, unique = true)
    private String slug;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Short orden;

    protected Categoria() {
    }

    public Categoria(String slug, String nombre, Short orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getNombre() {
        return nombre;
    }

    public Short getOrden() {
        return orden;
    }

    public void actualizar(String slug, String nombre, Short orden) {
        this.slug = slug;
        this.nombre = nombre;
        this.orden = orden;
    }
}
