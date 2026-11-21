package com.example.katEye.modulos.catalogo.oferta.entity;

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
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "oferta")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "precio_anterior", precision = 10, scale = 0, nullable = false)
    private BigDecimal precioAnterior;

    @Column(name = "precio_oferta", precision = 10, scale = 0, nullable = false)
    private BigDecimal precioOferta;

    @Column(name = "desde", nullable = false)
    private Instant desde;

    @Column(name = "hasta", nullable = false)
    private Instant hasta;

    protected Oferta() {
    }

    public Oferta(
            Producto producto,
            BigDecimal precioAnterior,
            BigDecimal precioOferta,
            Instant desde,
            Instant hasta) {
        this.producto = producto;
        this.precioAnterior = precioAnterior;
        this.precioOferta = precioOferta;
        this.desde = desde;
        this.hasta = hasta;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public BigDecimal getPrecioAnterior() {
        return precioAnterior;
    }

    public BigDecimal getPrecioOferta() {
        return precioOferta;
    }

    public Instant getDesde() {
        return desde;
    }

    public Instant getHasta() {
        return hasta;
    }

    public boolean estaVigente(Instant ahora) {
        return !desde.isAfter(ahora) && hasta.isAfter(ahora);
    }

    public void actualizar(
            Producto producto,
            BigDecimal precioAnterior,
            BigDecimal precioOferta,
            Instant desde,
            Instant hasta) {
        this.producto = producto;
        this.precioAnterior = precioAnterior;
        this.precioOferta = precioOferta;
        this.desde = desde;
        this.hasta = hasta;
    }
}
