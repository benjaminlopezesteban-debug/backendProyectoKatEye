package com.example.katEye.modulos.pagos.pago.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "transaccion_pago")
public class TransaccionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "proveedor", length = 50, nullable = false)
    private String proveedor;

    @Column(name = "referencia_externa", length = 100, nullable = false, unique = true)
    private String referenciaExterna;

    @Column(name = "monto", precision = 10, scale = 0, nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoPago estado;

    @Column(name = "codigo_respuesta", length = 50)
    private String codigoRespuesta;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "idempotency_key", length = 64, unique = true)
    private String idempotencyKey;

    protected TransaccionPago() {
    }

    public TransaccionPago(
            Long pedidoId,
            String proveedor,
            String referenciaExterna,
            BigDecimal monto,
            EstadoPago estado,
            String codigoRespuesta,
            String motivo,
            String idempotencyKey) {
        this.pedidoId = pedidoId;
        this.proveedor = proveedor;
        this.referenciaExterna = referenciaExterna;
        this.monto = monto;
        this.estado = estado;
        this.codigoRespuesta = codigoRespuesta;
        this.motivo = motivo;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
