package com.example.katEye.modulos.pedidos.pedidoproducto.entity;

import com.example.katEye.modulos.pedidos.pedido.entity.Pedido;
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
@Table(name = "pedido_producto")
public class PedidoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "nombre_snapshot", length = 100, nullable = false)
    private String nombreSnapshot;

    @Column(name = "precio_unitario", precision = 10, scale = 0, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "cantidad", nullable = false)
    private Short cantidad;

    protected PedidoProducto() {
    }

    public PedidoProducto(
            Pedido pedido,
            Long productoId,
            String nombreSnapshot,
            BigDecimal precioUnitario,
            Short cantidad) {
        this.pedido = pedido;
        this.productoId = productoId;
        this.nombreSnapshot = nombreSnapshot;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombreSnapshot() {
        return nombreSnapshot;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Short getCantidad() {
        return cantidad;
    }
}
