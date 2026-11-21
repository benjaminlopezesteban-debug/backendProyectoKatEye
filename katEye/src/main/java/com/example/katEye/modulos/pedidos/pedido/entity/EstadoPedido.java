package com.example.katEye.modulos.pedidos.pedido.entity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum EstadoPedido {

    PENDIENTE_PAGO,
    PAGO_RECHAZADO,
    PAGADO,
    EN_PREPARACION,
    ENVIADO,
    LISTO_PARA_RETIRO,
    ENTREGADO,
    EXPIRADO,
    CANCELADO,
    DEVUELTO;

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES =
            new EnumMap<>(EstadoPedido.class);

    static {
        TRANSICIONES.put(PENDIENTE_PAGO, EnumSet.of(PAGADO, PAGO_RECHAZADO, EXPIRADO, CANCELADO));
        TRANSICIONES.put(PAGO_RECHAZADO, EnumSet.of(PENDIENTE_PAGO, CANCELADO));
        TRANSICIONES.put(PAGADO, EnumSet.of(EN_PREPARACION, CANCELADO, DEVUELTO));
        TRANSICIONES.put(EN_PREPARACION, EnumSet.of(ENVIADO, LISTO_PARA_RETIRO, CANCELADO));
        TRANSICIONES.put(ENVIADO, EnumSet.of(ENTREGADO, DEVUELTO));
        TRANSICIONES.put(LISTO_PARA_RETIRO, EnumSet.of(ENTREGADO, CANCELADO));
        TRANSICIONES.put(ENTREGADO, EnumSet.of(DEVUELTO));
        TRANSICIONES.put(EXPIRADO, EnumSet.noneOf(EstadoPedido.class));
        TRANSICIONES.put(CANCELADO, EnumSet.noneOf(EstadoPedido.class));
        TRANSICIONES.put(DEVUELTO, EnumSet.noneOf(EstadoPedido.class));
    }

    public boolean puedePasarA(EstadoPedido destino) {
        return TRANSICIONES.get(this).contains(destino);
    }

    public boolean esFinal() {
        return TRANSICIONES.get(this).isEmpty();
    }
}
