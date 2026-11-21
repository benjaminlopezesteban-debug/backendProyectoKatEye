package com.example.katEye.modulos.catalogo.producto.entity;

import com.example.katEye.modulos.catalogo.categoria.entity.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio", precision = 10, scale = 0, nullable = false)
    private BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "stock_critico")
    private Integer stockCritico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected Producto() {
    }

    public Producto(
            String codigo,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stock,
            Integer stockCritico,
            Categoria categoria,
            boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.stockCritico = stockCritico;
        this.categoria = categoria;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getStockCritico() {
        return stockCritico;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public boolean isActivo() {
        return activo;
    }

    public void actualizar(
            String codigo,
            String nombre,
            String descripcion,
            BigDecimal precio,
            Integer stockCritico,
            Categoria categoria,
            boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stockCritico = stockCritico;
        this.categoria = categoria;
        this.activo = activo;
    }

    public void cambiarStock(Integer stock) {
        this.stock = stock;
    }

    public void desactivar() {
        this.activo = false;
    }
}
