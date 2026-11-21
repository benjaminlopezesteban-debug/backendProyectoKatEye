package com.example.katEye.modulos.catalogo.movimientostock.entity;

import com.example.katEye.modulos.catalogo.producto.entity.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimiento_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoMovimiento tipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    protected MovimientoStock() {
    }

    public MovimientoStock(
            Producto producto,
            TipoMovimiento tipo,
            Integer cantidad,
            String motivo,
            Long pedidoId,
            Long usuarioId) {
        this.producto = producto;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
