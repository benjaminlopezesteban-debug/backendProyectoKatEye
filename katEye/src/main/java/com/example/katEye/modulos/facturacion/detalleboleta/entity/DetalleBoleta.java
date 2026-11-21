package com.example.katEye.modulos.facturacion.detalleboleta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.example.katEye.modulos.facturacion.boleta.entity.Boleta;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    @Column(name = "pedido_producto_id", nullable = false)
    private Long pedidoProductoId;

    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private Short cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 0, nullable = false)
    private BigDecimal precioUnitario;

    protected DetalleBoleta() {
    }

    public DetalleBoleta(
            Long pedidoProductoId,
            String descripcion,
            Short cantidad,
            BigDecimal precioUnitario) {
        this.pedidoProductoId = pedidoProductoId;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public void asignarBoleta(Boleta boleta) {
        this.boleta = boleta;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoProductoId() {
        return pedidoProductoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Short getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}
