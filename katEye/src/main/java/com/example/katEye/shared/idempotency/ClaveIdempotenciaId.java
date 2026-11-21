package com.example.katEye.shared.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClaveIdempotenciaId implements Serializable {

    @Column(name = "clave", length = 100, nullable = false)
    private String clave;

    @Column(name = "endpoint", length = 120, nullable = false)
    private String endpoint;

    protected ClaveIdempotenciaId() {
    }

    public ClaveIdempotenciaId(String clave, String endpoint) {
        this.clave = clave;
        this.endpoint = endpoint;
    }

    public String getClave() {
        return clave;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof ClaveIdempotenciaId that)) {
            return false;
        }
        return Objects.equals(clave, that.clave) && Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave, endpoint);
    }
}
