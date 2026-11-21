package com.example.katEye.shared.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveIdempotenciaRepository extends JpaRepository<ClaveIdempotencia, ClaveIdempotenciaId> {
}
