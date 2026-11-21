package com.example.katEye.modulos.facturacion.venta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "monto_neto", precision = 10, scale = 0, nullable = false)
    private BigDecimal montoNeto;

    @Column(name = "monto_iva", precision = 10, scale = 0, nullable = false)
    private BigDecimal montoIva;

    @Column(name = "total", precision = 10, scale = 0, nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoVenta estado;

    protected Venta() {
    }

    public Venta(
            Long pedidoId,
            Instant fecha,
            BigDecimal montoNeto,
            BigDecimal montoIva,
            BigDecimal total,
            EstadoVenta estado) {
        this.pedidoId = pedidoId;
        this.fecha = fecha;
        this.montoNeto = montoNeto;
        this.montoIva = montoIva;
        this.total = total;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public BigDecimal getMontoNeto() {
        return montoNeto;
    }

    public BigDecimal getMontoIva() {
        return montoIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void anular() {
        this.estado = EstadoVenta.ANULADA;
    }
}
