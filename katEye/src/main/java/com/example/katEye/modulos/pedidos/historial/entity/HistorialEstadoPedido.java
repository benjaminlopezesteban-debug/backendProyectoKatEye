package com.example.katEye.modulos.pedidos.historial.entity;

import com.example.katEye.modulos.pedidos.pedido.entity.EstadoPedido;
import com.example.katEye.modulos.pedidos.pedido.entity.Pedido;
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
import java.time.Instant;

@Entity
@Table(name = "historial_estado_pedido")
public class HistorialEstadoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 25)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", length = 25, nullable = false)
    private EstadoPedido estadoNuevo;

    @Column(name = "actor_usuario_id")
    private Long actorUsuarioId;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    protected HistorialEstadoPedido() {
    }

    public HistorialEstadoPedido(
            Pedido pedido,
            EstadoPedido estadoAnterior,
            EstadoPedido estadoNuevo,
            Long actorUsuarioId,
            String motivo) {
        this.pedido = pedido;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.actorUsuarioId = actorUsuarioId;
        this.motivo = motivo;
        this.fecha = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public EstadoPedido getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoPedido getEstadoNuevo() {
        return estadoNuevo;
    }

    public Long getActorUsuarioId() {
        return actorUsuarioId;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getFecha() {
        return fecha;
    }
}
