package com.example.katEye.modulos.catalogo.productoimagen.entity;

import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "producto_imagen")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "orden", nullable = false)
    private Short orden;

    protected ProductoImagen() {
    }

    public ProductoImagen(Producto producto, String url, Short orden) {
        this.producto = producto;
        this.url = url;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public String getUrl() {
        return url;
    }

    public Short getOrden() {
        return orden;
    }
}
