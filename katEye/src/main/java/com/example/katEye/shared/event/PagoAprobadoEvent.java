package com.example.katEye.shared.event;

import java.util.List;

public record PagoAprobadoEvent(Long pedidoId, Long pagoId, List<ItemEvento> items) {
}
