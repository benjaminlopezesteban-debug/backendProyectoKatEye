package com.example.katEye.shared.idempotency;

import com.example.katEye.shared.exception.SolicitudInvalidaException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotenciaService {

    private final ClaveIdempotenciaRepository repository;

    public IdempotenciaService(ClaveIdempotenciaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Long> buscar(String clave, String endpoint) {
        validar(clave, endpoint);
        return repository.findById(new ClaveIdempotenciaId(clave, endpoint))
                .map(ClaveIdempotencia::getReferenciaId);
    }

    @Transactional
    public void registrar(String clave, String endpoint, Long referenciaId) {
        validar(clave, endpoint);
        if (referenciaId == null) {
            throw new SolicitudInvalidaException("La referencia de idempotencia es obligatoria");
        }
        repository.save(new ClaveIdempotencia(clave, endpoint, referenciaId));
    }

    private void validar(String clave, String endpoint) {
        if (clave == null || clave.isBlank()) {
            throw new SolicitudInvalidaException("El header Idempotency-Key es obligatorio");
        }
        if (endpoint == null || endpoint.isBlank()) {
            throw new SolicitudInvalidaException("El endpoint de idempotencia es obligatorio");
        }
    }
}
