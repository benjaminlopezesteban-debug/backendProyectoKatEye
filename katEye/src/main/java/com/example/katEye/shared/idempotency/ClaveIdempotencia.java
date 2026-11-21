package com.example.katEye.shared.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "clave_idempotencia")
public class ClaveIdempotencia {

    @EmbeddedId
    private ClaveIdempotenciaId id;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected ClaveIdempotencia() {
    }

    public ClaveIdempotencia(String clave, String endpoint, Long referenciaId) {
        this.id = new ClaveIdempotenciaId(clave, endpoint);
        this.referenciaId = referenciaId;
        this.creadoEn = Instant.now();
    }

    public ClaveIdempotenciaId getId() {
        return id;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
