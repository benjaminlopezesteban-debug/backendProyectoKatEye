-- V1: esquema inicial KatEye (MySQL 8.4)
-- Equivalente al modelo logico Oracle (docs/proyecto/modeloNegocio.sql)

CREATE TABLE region (
    codigo VARCHAR(10) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE comuna (
    codigo        VARCHAR(10)  PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    region_codigo VARCHAR(10)  NOT NULL,
    CONSTRAINT uq_comuna_nombre UNIQUE (region_codigo, nombre),
    CONSTRAINT fk_comuna_region FOREIGN KEY (region_codigo) REFERENCES region (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE rol (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE usuario (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    correo        VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono      VARCHAR(20),
    region_codigo VARCHAR(10)  NOT NULL,
    comuna_codigo VARCHAR(10)  NOT NULL,
    rol_id        BIGINT       NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_region FOREIGN KEY (region_codigo) REFERENCES region (codigo),
    CONSTRAINT fk_usuario_comuna FOREIGN KEY (comuna_codigo) REFERENCES comuna (codigo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_usuario_region ON usuario (region_codigo);
CREATE INDEX idx_usuario_comuna ON usuario (comuna_codigo);
CREATE INDEX idx_usuario_rol ON usuario (rol_id);

CREATE TABLE recuperacion (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT       NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expira_en  DATETIME(6)  NOT NULL,
    CONSTRAINT uq_recuperacion_token UNIQUE (token_hash),
    CONSTRAINT fk_recuperacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_recuperacion_usuario ON recuperacion (usuario_id);

CREATE TABLE intento_acceso (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT,
    tipo       VARCHAR(20) NOT NULL,
    correo     VARCHAR(100),
    ip         VARCHAR(45),
    exitoso    BOOLEAN     NOT NULL DEFAULT FALSE,
    fecha      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_intento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_intento_tipo CHECK (tipo IN ('LOGIN', 'REGISTRO', 'RECUPERACION', 'CONTACTO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_intento_usuario ON intento_acceso (usuario_id);
CREATE INDEX idx_intento_fecha ON intento_acceso (fecha);

CREATE TABLE categoria (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug   VARCHAR(60)  NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    orden  SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_categoria_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE producto (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo        VARCHAR(20)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    descripcion   VARCHAR(500),
    precio        DECIMAL(10,0) NOT NULL DEFAULT 0,
    stock         INTEGER      NOT NULL DEFAULT 0,
    stock_critico INTEGER,
    categoria_id  BIGINT       NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT chk_producto_codigo CHECK (CHAR_LENGTH(codigo) >= 3),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_stock_crit CHECK (stock_critico IS NULL OR stock_critico >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_producto_categoria ON producto (categoria_id);

CREATE TABLE producto_imagen (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT       NOT NULL,
    url         VARCHAR(500) NOT NULL,
    orden       SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uq_producto_imagen_orden UNIQUE (producto_id, orden),
    CONSTRAINT fk_producto_imagen_prod FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE oferta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id     BIGINT       NOT NULL,
    precio_anterior DECIMAL(10,0) NOT NULL,
    precio_oferta   DECIMAL(10,0) NOT NULL,
    desde           DATETIME(6)  NOT NULL,
    hasta           DATETIME(6)  NOT NULL,
    CONSTRAINT fk_oferta_producto FOREIGN KEY (producto_id) REFERENCES producto (id) ON DELETE CASCADE,
    CONSTRAINT chk_oferta_precios CHECK (precio_anterior >= 0 AND precio_oferta >= 0),
    CONSTRAINT chk_oferta_descuento CHECK (precio_oferta <= precio_anterior),
    CONSTRAINT chk_oferta_vigencia CHECK (hasta > desde)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_oferta_producto ON oferta (producto_id);

CREATE TABLE carrito (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id    BIGINT,
    token_anonimo VARCHAR(64),
    CONSTRAINT uq_carrito_usuario UNIQUE (usuario_id),
    CONSTRAINT uq_carrito_token UNIQUE (token_anonimo),
    CONSTRAINT fk_carrito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT chk_carrito_duenio CHECK (usuario_id IS NOT NULL OR token_anonimo IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE carrito_item (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    carrito_id  BIGINT   NOT NULL,
    producto_id BIGINT   NOT NULL,
    cantidad    SMALLINT NOT NULL DEFAULT 1,
    CONSTRAINT uq_carrito_item UNIQUE (carrito_id, producto_id),
    CONSTRAINT fk_carrito_item_carrito FOREIGN KEY (carrito_id) REFERENCES carrito (id) ON DELETE CASCADE,
    CONSTRAINT fk_carrito_item_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT chk_carrito_item_cantidad CHECK (cantidad BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_carrito_item_producto ON carrito_item (producto_id);

CREATE TABLE pedido (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero             VARCHAR(20)  NOT NULL,
    usuario_id         BIGINT,
    tipo_entrega       VARCHAR(20)  NOT NULL,
    contacto_nombre    VARCHAR(100) NOT NULL,
    contacto_correo    VARCHAR(100) NOT NULL,
    contacto_telefono  VARCHAR(20)  NOT NULL,
    direccion          VARCHAR(300),
    region_codigo      VARCHAR(10),
    comuna_codigo      VARCHAR(10),
    metodo_pago        VARCHAR(20)  NOT NULL,
    estado             VARCHAR(25)  NOT NULL,
    subtotal           DECIMAL(10,0) NOT NULL DEFAULT 0,
    ahorro             DECIMAL(10,0) NOT NULL DEFAULT 0,
    costo_despacho     DECIMAL(10,0) NOT NULL DEFAULT 0,
    total              DECIMAL(10,0) NOT NULL,
    transportista      VARCHAR(100),
    codigo_seguimiento VARCHAR(60),
    idempotency_key    VARCHAR(64),
    expira_en          DATETIME(6),
    CONSTRAINT uq_pedido_numero UNIQUE (numero),
    CONSTRAINT uq_pedido_idempotencia UNIQUE (idempotency_key),
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_pedido_region FOREIGN KEY (region_codigo) REFERENCES region (codigo),
    CONSTRAINT fk_pedido_comuna FOREIGN KEY (comuna_codigo) REFERENCES comuna (codigo),
    CONSTRAINT chk_pedido_entrega CHECK (tipo_entrega IN ('DESPACHO', 'RETIRO')),
    CONSTRAINT chk_pedido_pago CHECK (metodo_pago IN ('TARJETA', 'TRANSFERENCIA')),
    CONSTRAINT chk_pedido_estado CHECK (estado IN (
        'PENDIENTE_PAGO', 'PAGO_RECHAZADO', 'PAGADO', 'EN_PREPARACION',
        'ENVIADO', 'LISTO_PARA_RETIRO', 'ENTREGADO', 'EXPIRADO',
        'CANCELADO', 'DEVUELTO')),
    CONSTRAINT chk_pedido_montos CHECK (
        subtotal >= 0 AND ahorro >= 0 AND costo_despacho >= 0 AND total >= 0),
    CONSTRAINT chk_pedido_despacho CHECK (
        tipo_entrega = 'RETIRO'
        OR (direccion IS NOT NULL AND region_codigo IS NOT NULL AND comuna_codigo IS NOT NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_pedido_usuario ON pedido (usuario_id);
CREATE INDEX idx_pedido_estado ON pedido (estado);
CREATE INDEX idx_pedido_fecha ON pedido (expira_en);

CREATE TABLE pedido_producto (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       BIGINT       NOT NULL,
    producto_id     BIGINT       NOT NULL,
    nombre_snapshot VARCHAR(100) NOT NULL,
    precio_unitario DECIMAL(10,0) NOT NULL,
    cantidad        SMALLINT     NOT NULL,
    CONSTRAINT uq_pedido_producto UNIQUE (pedido_id, producto_id),
    CONSTRAINT fk_pedido_producto_ped FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_producto_prd FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT chk_pedido_producto_cant CHECK (cantidad BETWEEN 1 AND 10),
    CONSTRAINT chk_pedido_producto_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_pedido_producto_prd ON pedido_producto (producto_id);

CREATE TABLE historial_estado_pedido (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id        BIGINT      NOT NULL,
    estado_anterior  VARCHAR(25),
    estado_nuevo     VARCHAR(25) NOT NULL,
    actor_usuario_id BIGINT,
    motivo           VARCHAR(300),
    fecha            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_historial_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (actor_usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_historial_pedido ON historial_estado_pedido (pedido_id);
CREATE INDEX idx_historial_actor ON historial_estado_pedido (actor_usuario_id);

CREATE TABLE movimiento_stock (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT      NOT NULL,
    tipo        VARCHAR(20) NOT NULL,
    cantidad    INTEGER     NOT NULL,
    motivo      VARCHAR(200),
    pedido_id   BIGINT,
    usuario_id  BIGINT,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_movimiento_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_movimiento_tipo CHECK (tipo IN ('ENTRADA', 'SALIDA', 'RESERVA', 'LIBERACION', 'AJUSTE')),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_movimiento_producto ON movimiento_stock (producto_id);
CREATE INDEX idx_movimiento_pedido ON movimiento_stock (pedido_id);
CREATE INDEX idx_movimiento_usuario ON movimiento_stock (usuario_id);

CREATE TABLE transaccion_pago (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id          BIGINT       NOT NULL,
    proveedor          VARCHAR(50)  NOT NULL,
    referencia_externa VARCHAR(100) NOT NULL,
    monto              DECIMAL(10,0) NOT NULL,
    estado             VARCHAR(20)  NOT NULL,
    codigo_respuesta   VARCHAR(50),
    motivo             VARCHAR(300),
    idempotency_key    VARCHAR(64),
    CONSTRAINT uq_transaccion_ref UNIQUE (referencia_externa),
    CONSTRAINT uq_transaccion_idemp UNIQUE (idempotency_key),
    CONSTRAINT fk_transaccion_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT chk_transaccion_estado CHECK (estado IN ('INICIADA', 'APROBADA', 'RECHAZADA', 'PENDIENTE', 'ANULADA')),
    CONSTRAINT chk_transaccion_monto CHECK (monto >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_transaccion_pedido ON transaccion_pago (pedido_id);

CREATE TABLE venta (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id  BIGINT       NOT NULL,
    fecha      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    monto_neto DECIMAL(10,0) NOT NULL,
    monto_iva  DECIMAL(10,0) NOT NULL,
    total      DECIMAL(10,0) NOT NULL,
    estado     VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_venta_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_venta_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT chk_venta_estado CHECK (estado IN ('PENDIENTE', 'EMITIDA', 'ANULADA')),
    CONSTRAINT chk_venta_montos CHECK (
        monto_neto >= 0 AND monto_iva >= 0 AND total >= 0
        AND total = monto_neto + monto_iva)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE boleta (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id       BIGINT       NOT NULL,
    folio          VARCHAR(30)  NOT NULL,
    tipo_documento VARCHAR(10)  NOT NULL,
    rut_receptor   VARCHAR(12),
    fecha_emision  DATETIME(6)  NOT NULL,
    monto_neto     DECIMAL(10,0) NOT NULL,
    monto_iva      DECIMAL(10,0) NOT NULL,
    total          DECIMAL(10,0) NOT NULL,
    estado         VARCHAR(15)  NOT NULL,
    CONSTRAINT uq_boleta_venta UNIQUE (venta_id),
    CONSTRAINT uq_boleta_folio UNIQUE (folio),
    CONSTRAINT fk_boleta_venta FOREIGN KEY (venta_id) REFERENCES venta (id),
    CONSTRAINT chk_boleta_tipo CHECK (tipo_documento IN ('BOLETA', 'FACTURA')),
    CONSTRAINT chk_boleta_estado CHECK (estado IN ('EMITIDA', 'ANULADA')),
    CONSTRAINT chk_boleta_montos CHECK (
        monto_neto >= 0 AND monto_iva >= 0 AND total >= 0
        AND total = monto_neto + monto_iva)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE detalle_boleta (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    boleta_id          BIGINT       NOT NULL,
    pedido_producto_id BIGINT       NOT NULL,
    descripcion        VARCHAR(200) NOT NULL,
    cantidad           SMALLINT     NOT NULL,
    precio_unitario    DECIMAL(10,0) NOT NULL,
    CONSTRAINT uq_detalle_boleta UNIQUE (boleta_id, pedido_producto_id),
    CONSTRAINT fk_detalle_boleta FOREIGN KEY (boleta_id) REFERENCES boleta (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_pedprod FOREIGN KEY (pedido_producto_id) REFERENCES pedido_producto (id),
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_detalle_pedprod ON detalle_boleta (pedido_producto_id);

CREATE TABLE entradas_blog (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    autor_usuario_id BIGINT       NOT NULL,
    slug             VARCHAR(100) NOT NULL,
    titulo           VARCHAR(200) NOT NULL,
    resumen          VARCHAR(500),
    imagen_url       VARCHAR(500),
    bloques          JSON,
    publicada        BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_entradas_blog_slug UNIQUE (slug),
    CONSTRAINT fk_blog_usuario FOREIGN KEY (autor_usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_blog_autor ON entradas_blog (autor_usuario_id);

CREATE TABLE mensaje_contacto (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    respondido_por BIGINT,
    nombre         VARCHAR(100) NOT NULL,
    correo         VARCHAR(100) NOT NULL,
    comentario     VARCHAR(500) NOT NULL,
    estado         VARCHAR(15)  NOT NULL DEFAULT 'NUEVO',
    CONSTRAINT fk_contacto_usuario FOREIGN KEY (respondido_por) REFERENCES usuario (id),
    CONSTRAINT chk_contacto_estado CHECK (estado IN ('NUEVO', 'LEIDO', 'RESPONDIDO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE INDEX idx_contacto_respondido ON mensaje_contacto (respondido_por);

CREATE TABLE clave_idempotencia (
    clave         VARCHAR(100) NOT NULL,
    endpoint      VARCHAR(120) NOT NULL,
    referencia_id BIGINT       NOT NULL,
    creado_en     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_clave_idempotencia PRIMARY KEY (clave, endpoint)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
