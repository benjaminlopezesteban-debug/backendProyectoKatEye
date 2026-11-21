package com.example.katEye.shared.event;

import java.util.List;

public record PagoRechazadoEvent(Long pedidoId, String motivo, List<ItemEvento> items) {
}
