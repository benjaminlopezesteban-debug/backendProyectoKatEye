# KatEye — Arquitectura del backend MVP (Spring Boot, monolito modular)

> Documento de contexto para desarrollo agéntico. Define estructura de carpetas, dependencias permitidas, patrones y reglas.
> Los números entre paréntesis (1)–(18) remiten a los lineamientos de diseño acordados.
> En los árboles, `controller/X.java` significa subcarpeta `controller/` + archivo `X.java`.
> Alcance: **modelo completo de 22 tablas** (`modeloNegocio.sql`), implementado como monolito modular.

## 1. Lineamientos de diseño

1. Arquitectura monolito modular, un único módulo Spring Boot.
2. Separación de responsabilidades por dominio en módulos distintos.
3. Módulos compartidos a nivel de aplicación (`shared`): Config, Security, Exception, Event, Idempotency y DTO comunes.
4. Cada entidad principal tiene sus archivos: controller, service, repository, entity, dto y exception. Las entidades hijas no tienen controller propio.
5. Sin mapper y sin clases `Impl`; el DTO vive junto a su entidad y la conversión se hace con métodos privados dentro del Service.
6. Estados manejados con clases `enum` y `@Enumerated(STRING)`.
7. Orquestador para lógicas que coordinan más de 3 services.
8. Sin patrón Strategy: el pago se simula con un endpoint y el resultado lo decide el servidor.
9. Sin Specification/Criteria: filtros con un `@Query` de parámetros opcionales.
10. Observer con eventos de Spring para evitar dependencias circulares.
11. Idempotency key con lógica simple y entidad en `shared`.
12. Backend en contenedor Docker.
13. Base de datos en otro contenedor.
14. Frontend en un tercer contenedor (React + Nginx).
15. Esquema versionado con Flyway y datos semilla.
16. API REST versionada en `/api/v1`.
17. Seguridad JWT con access token puro, sin refresh token.
18. Pedido con los 10 estados del ERS §3.4; pago simulado por configuración del servidor.

## 2. Raíz del proyecto

```
KatEyeBackEnd/
├── AGENTS.md                        # reglas de arquitectura para el agente (sección 5)
├── README.md
├── docker-compose.yml               # (12)(13)(14) backend + db + frontend
├── .env.example                     # MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD, JWT_SECRET, CORS_ORIGEN, PAGO_SIMULADO
├── .dockerignore
├── .gitignore
├── frontend/                        # (14) contenedor React + Nginx (build o repo externo)
├── katEye/                          # módulo Spring Boot
│   ├── pom.xml
│   ├── Dockerfile                   # multi-stage: Maven (build) → imagen JRE (run)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/katEye/   # ver sección 3
│       │   └── resources/
│       │       ├── application.yml        # configuración única, lee variables de entorno
│       │       └── db/migration/          # (15) Flyway
│       │           ├── V1__esquema.sql
│       │           ├── V2__seed_ubicacion.sql
│       │           ├── V3__seed_admin.sql
│       │           ├── V4__seed_catalogo.sql
│       │           ├── V5__contenido_fechas.sql
│       │           └── V6__seed_contenido.sql
│       └── test/java/com/example/katEye/
│           ├── modulos/catalogo/producto/ProductoServiceTest.java
│           ├── modulos/pagos/pago/PagoServiceTest.java
│           ├── orquestador/checkout/CheckoutOrquestadorTest.java
│           └── shared/idempotency/IdempotenciaServiceTest.java
└── docs/proyecto/
    ├── requirements.md
    ├── ARQUITECTURA_BACKEND.md
    ├── modeloNegocio.sql
    └── plan_desarrollo.md
```

## 3. Código Java (`com.example.katEye`)

```
com.example.katEye/
├── KatEyeApplication.java
│
├── shared/                                   # (3) transversal; no depende de ningún módulo
│   ├── config/
│   │   └── CorsConfig.java                   # origen del frontend React
│   ├── security/
│   │   ├── SecurityConfig.java               # rutas públicas / por rol, BCrypt, @EnableMethodSecurity
│   │   ├── JwtService.java                   # genera y valida el token
│   │   ├── JwtAuthFilter.java                # token → usuario autenticado (sin consultar la BD)
│   │   ├── Rol.java                          # enum ADMINISTRADOR | VENDEDOR | CLIENTE
│   │   └── SecurityUtils.java                # usuarioActualId(), rolActual()
│   ├── exception/
│   │   ├── RecursoNoEncontradoException.java # base → 404
│   │   ├── ReglaNegocioException.java        # base → 422
│   │   ├── ConflictoException.java           # base → 409
│   │   ├── SolicitudInvalidaException.java   # base → 400
│   │   ├── NoAutorizadoException.java        # base → 401
│   │   ├── ErrorResponse.java                # record: formato único de error
│   │   └── GlobalExceptionHandler.java       # @RestControllerAdvice
│   ├── event/                                # (10) contratos del observer: solo records
│   │   ├── ItemEvento.java                   # productoId, cantidad
│   │   ├── PagoAprobadoEvent.java            # pedidoId, pagoId, List<ItemEvento>
│   │   └── PagoRechazadoEvent.java           # pedidoId, motivo, List<ItemEvento>
│   ├── idempotency/                          # (11)
│   │   ├── ClaveIdempotencia.java            # entidad: (clave, endpoint) PK, referenciaId, creadoEn
│   │   ├── ClaveIdempotenciaRepository.java
│   │   └── IdempotenciaService.java          # buscar(clave, endpoint) / registrar(clave, endpoint, refId)
│   └── dto/
│       ├── ApiResponse.java                  # envoltura de respuesta
│       └── PageResponse.java                 # paginación uniforme
│
├── modulos/                                  # (2)(4) un módulo por dominio
│   │
│   ├── ubicacion/
│   │   ├── region/{controller/RegionController.java, service/RegionService.java,
│   │   │            repository/RegionRepository.java, entity/Region.java,
│   │   │            dto/RegionResponse.java, exception/RegionNoEncontradaException.java}
│   │   └── comuna/{controller/ComunaController.java, service/ComunaService.java,
│   │                repository/ComunaRepository.java, entity/Comuna.java,
│   │                dto/ComunaResponse.java, exception/ComunaNoEncontradaException.java}
│   │
│   ├── usuarios/
│   │   ├── usuario/{controller/UsuarioController.java, service/UsuarioService.java,
│   │   │            repository/UsuarioRepository.java, entity/Usuario.java,
│   │   │            dto/UsuarioRequest.java,
│   │   │            dto/UsuarioResponse.java, exception/UsuarioNoEncontradoException.java,
│   │   │            exception/CorreoYaRegistradoException.java}
│   │   ├── rol/{controller/RolController.java, service/RolService.java,
│   │   │        repository/RolRepository.java, entity/RolEntidad.java,
│   │   │        dto/RolResponse.java, exception/RolNoEncontradoException.java}
│   │   ├── auth/{controller/AuthController.java, service/AuthService.java,
│   │   │         dto/LoginRequest.java, dto/RegistroRequest.java,
│   │   │         dto/TokenResponse.java, exception/CredencialesInvalidasException.java}
│   │   ├── recuperacion/{controller/RecuperacionController.java, service/RecuperacionService.java,
│   │   │                 repository/RecuperacionRepository.java, entity/Recuperacion.java,
│   │   │                 dto/RecuperacionRequest.java, exception/TokenInvalidoException.java}
│   │   └── intentoacceso/{controller/IntentoAccesoController.java, service/IntentoAccesoService.java,
│   │                      repository/IntentoAccesoRepository.java, entity/IntentoAcceso.java,
│   │                      dto/IntentoAccesoResponse.java}
│   │
│   ├── catalogo/
│   │   ├── categoria/{controller/CategoriaController.java, service/CategoriaService.java,
│   │   │               repository/CategoriaRepository.java, entity/Categoria.java,
│   │   │               dto/CategoriaResponse.java, exception/CategoriaNoEncontradaException.java}
│   │   ├── producto/{controller/ProductoController.java, service/ProductoService.java,
│   │   │             repository/ProductoRepository.java, entity/Producto.java,
│   │   │             listener/StockPagoListener.java,
│   │   │             dto/ProductoRequest.java, dto/ProductoResponse.java,
│   │   │             exception/ProductoNoEncontradoException.java,
│   │   │             exception/StockInsuficienteException.java}
│   │   ├── productoimagen/{controller/ProductoImagenController.java, service/ProductoImagenService.java,
│   │   │                    repository/ProductoImagenRepository.java, entity/ProductoImagen.java,
│   │   │                    dto/ProductoImagenResponse.java, exception/ImagenNoEncontradaException.java}
│   │   ├── oferta/{controller/OfertaController.java, service/OfertaService.java,
│   │   │          repository/OfertaRepository.java, entity/Oferta.java,
│   │   │          dto/OfertaRequest.java, dto/OfertaResponse.java,
│   │   │          exception/OfertaNoEncontradaException.java}
│   │   └── movimientostock/{controller/MovimientoStockController.java, service/MovimientoStockService.java,
│   │                        repository/MovimientoStockRepository.java, entity/MovimientoStock.java,
│   │                        entity/TipoMovimiento.java, dto/MovimientoStockResponse.java}
│   │
│   ├── carrito/
│   │   ├── carrito/{controller/CarritoController.java, service/CarritoService.java,
│   │   │            repository/CarritoRepository.java, entity/Carrito.java,
│   │   │            dto/CarritoResponse.java, dto/CarritoItemResponse.java,
│   │   │            dto/ItemCarritoRequest.java, dto/ActualizarCantidadRequest.java,
│   │   │            dto/ValidarCarritoRequest.java, dto/CarritoValidacionResponse.java,
│   │   │            exception/CarritoNoEncontradoException.java}
│   │   └── carritoitem/{entity/CarritoItem.java, repository/CarritoItemRepository.java,
│   │                    exception/ItemCarritoNoEncontradoException.java}
│   │
│   ├── pedidos/
│   │   ├── pedido/{controller/PedidoController.java, service/PedidoService.java,
│   │   │           repository/PedidoRepository.java, entity/Pedido.java,
│   │   │           entity/EstadoPedido.java, entity/TipoEntrega.java, entity/MetodoPago.java,
│   │   │           listener/PagoPedidoListener.java,
│   │   │           dto/PedidoResponse.java, dto/PedidoProductoResponse.java,
│   │   │           dto/PedidoPagoResponse.java, dto/PedidoFacturacionResponse.java,
│   │   │           dto/CambiarEstadoRequest.java, dto/SeguimientoRequest.java,
│   │   │           dto/PedidoCommand.java, dto/LineaPedidoCommand.java,
│   │   │           exception/PedidoNoEncontradoException.java,
│   │   │           exception/TransicionEstadoInvalidaException.java}
│   │   ├── pedidoproducto/{entity/PedidoProducto.java, repository/PedidoProductoRepository.java,
│   │   │                    dto/PedidoProductoResponse.java}
│   │   └── historial/{service/HistorialEstadoService.java,
│   │                   repository/HistorialEstadoPedidoRepository.java,
│   │                   entity/HistorialEstadoPedido.java, dto/HistorialEstadoResponse.java}
│   │
│   ├── pagos/
│   │   └── pago/{controller/PagoController.java, service/PagoService.java,
│   │            repository/PagoRepository.java, entity/TransaccionPago.java,
│   │            entity/EstadoPago.java,
│   │            dto/PagoRequest.java, dto/PagoResponse.java,
│   │            exception/PedidoNoPagableException.java,
│   │            exception/PagoNoEncontradoException.java}
│   │
│   ├── facturacion/
│   │   ├── venta/{controller/VentaController.java, service/VentaService.java,
│   │   │         repository/VentaRepository.java, entity/Venta.java,
│   │   │         entity/EstadoVenta.java,
│   │   │         dto/VentaResponse.java, exception/VentaNoEncontradaException.java}
│   │   ├── boleta/{controller/BoletaController.java, service/BoletaService.java,
│   │   │          repository/BoletaRepository.java, entity/Boleta.java,
│   │   │          entity/EstadoBoleta.java, entity/TipoDocumento.java,
│   │   │          dto/BoletaResponse.java, dto/EmitirBoletaRequest.java,
│   │   │          exception/BoletaNoEncontradaException.java}
│   │   ├── detalleboleta/{entity/DetalleBoleta.java, dto/DetalleBoletaResponse.java}
│   │   └── listener/PagoAprobadoFacturacionListener.java
│   │
│   └── contenido/
│       ├── entradablog/{controller/EntradaBlogController.java, service/EntradaBlogService.java,
│       │                repository/EntradaBlogRepository.java, entity/EntradaBlog.java,
│       │                dto/BloqueBlog.java, dto/ItemLista.java,
│       │                dto/EntradaBlogRequest.java, dto/EntradaBlogResponse.java,
│       │                dto/PublicacionRequest.java,
│       │                exception/EntradaNoEncontradaException.java,
│       │                exception/SlugEntradaDuplicadoException.java}
│       └── mensajecontacto/{controller/MensajeContactoController.java, service/MensajeContactoService.java,
│                            repository/MensajeContactoRepository.java, entity/MensajeContacto.java,
│                            entity/EstadoMensaje.java, dto/MensajeContactoRequest.java,
│                            dto/MensajeContactoResponse.java, dto/CambiarEstadoMensajeRequest.java,
│                            exception/MensajeNoEncontradoException.java}
│
└── orquestador/                              # (7) casos de uso con más de 3 services
    └── checkout/
        ├── controller/CheckoutController.java   # POST /api/v1/checkout [Idempotency-Key]
        ├── service/CheckoutOrquestador.java     # coordina carrito, producto, usuario, comuna, pedido, idempotencia
        ├── dto/CheckoutRequest.java             # ítems (productoId, cantidad), entrega, método de pago
        ├── dto/CheckoutResponse.java            # número de pedido, total, estado
        └── exception/CarritoInvalidoException.java
```

### Nota sobre `ROL`

`modeloNegocio.sql` define la tabla `ROL`, por lo que existe `rol/entity/RolEntidad.java` mapeada a `ROL`. La autorización usa el enum `shared/security/Rol.java`. La equivalencia es fija por seed:

| ID | RolEntidad | Enum de seguridad |
|---|---|---|
| 1 | Administrador | `ADMINISTRADOR` |
| 2 | Vendedor | `VENDEDOR` |
| 3 | Cliente | `CLIENTE` |

## 4. Persistencia y migraciones

- Un contenedor MySQL 8.4, una base `kateye`, sin esquema adicional (la propia base es el esquema).
- Flyway administra el esquema; `spring.jpa.hibernate.ddl-auto=validate`.
- `V1__esquema.sql` crea las 22 tablas de negocio más la tabla técnica `CLAVE_IDEMPOTENCIA` del módulo `shared/idempotency`.
- `V2`, `V3` y `V4` cargan datos semilla idempotentes (`INSERT IGNORE`): ubicación, catálogo y administrador.
- `V5__contenido_fechas.sql` agrega `entradas_blog.publicada_en` y `mensaje_contacto.creado_en`; `V6__seed_contenido.sql` siembra dos entradas de blog publicadas.
- Los bloques de blog se guardan en `entradas_blog.bloques` como `JSON`. La entidad usa `tools.jackson.databind.JsonNode` con `@JdbcTypeCode(SqlTypes.JSON)`; Hibernate 7.4 trabaja con Jackson 3 y se selecciona explícitamente con `hibernate.type.json_format_mapper=org.hibernate.type.format.jackson.Jackson3JsonFormatMapper`.
- `modeloNegocio.sql` (Oracle) es el modelo lógico de referencia para Data Modeler. Las migraciones Flyway son su equivalente MySQL:

| Oracle | MySQL |
|---|---|
| `NUMBER(19)` identity | `BIGINT AUTO_INCREMENT` |
| `VARCHAR2(n)` | `VARCHAR(n)` |
| `CHAR(1) 'S'/'N'` | `BOOLEAN` (alias de `TINYINT(1)`) |
| `TIMESTAMP` | `DATETIME(6)` |
| `CLOB` + `IS JSON` | `JSON` |
| `SYSTIMESTAMP` | `CURRENT_TIMESTAMP(6)` |
| `ON CONFLICT DO NOTHING` | `INSERT IGNORE` / `ON DUPLICATE KEY UPDATE` |

- No se publica el puerto de la base de datos; solo es accesible por la red interna de Compose.

## 5. Dependencias y flujos

### Dependencias permitidas

```
orquestador ─┬─► usuarios ───► ubicacion
             ├─► catalogo
             ├─► carrito
             └─► pedidos

pagos ─────────► pedidos
facturacion ───► pedidos

(todos los anteriores) ──► shared
```

Los eventos no crean flechas: `pagos` publica, `pedidos`, `catalogo` y `facturacion` escuchan. Ninguno conoce al otro; solo conocen `shared/event`.

### Checkout

```
POST /api/v1/checkout  [Idempotency-Key]
 └─ CheckoutOrquestador.confirmar()                @Transactional
     1. IdempotenciaService.buscar()      → si ya existe, devuelve el pedido anterior
     2. CarritoService.itemsDe/itemsMios() → líneas normalizadas (o ítems del request)
     3. ProductoService.verificarDisponibilidad() → stock real por ítem
     4. UsuarioService.obtenerEntidad()    → autocompleta contacto si hay sesión
     5. ComunaService.validarPertenenciaRegion()
     6. PedidoService.crear()              → estado PENDIENTE_PAGO + snapshots y totales en servidor
     7. MovimientoStockService.reservar()  → lock pesimista; descuenta stock de inmediato
     8. CarritoService.vaciar()            → si el checkout usó el carrito
     9. IdempotenciaService.registrar()
     └─► CheckoutResponse (numero, total, estado)
```

El número de pedido es `yyyyMMdd-####` con correlativo diario (por ejemplo `20261117-0001`). La consulta de invitado exige `correo` y solo expone pedidos sin usuario registrado.

Reservas de stock: `reservar` toma un lock pesimista del producto y descuenta `producto.stock` de inmediato; `liberar` repone el stock y registra `LIBERACION`; `confirmar` (al aprobarse el pago) solo registra `SALIDA`, sin descontar dos veces.

### Pago simulado

```
POST /api/v1/pagos/simular  [Idempotency-Key]  { pedidoId, metodo }
 └─ PagoService
     PedidoService.obtener() → exige PENDIENTE_PAGO
     resultado = kateye.pago.simulado-resultado   (APROBADA | RECHAZADA)  ← lo decide el servidor
     guarda TransaccionPago
     ├─ APROBADA  ─► PagoAprobadoEvent ─┬─► PagoPedidoListener (pedidos)      → PAGADO + historial
     │                                   ├─► StockPagoListener (catalogo)     → confirma reserva (SALIDA)
     │                                   └─► PagoAprobadoFacturacionListener → crea Venta y emite Boleta
     └─ RECHAZADA ─► PagoRechazadoEvent ─┬─► PagoPedidoListener                → PAGO_RECHAZADO + historial
                                          └─► StockPagoListener                → libera la reserva
```

El cliente **no** envía el resultado. El valor de `kateye.pago.simulado-resultado` permite demostrar el flujo de pago rechazado sin violar RNF-SEG-10.

### Estados del pedido (10)

`PENDIENTE_PAGO → PAGADO → EN_PREPARACION → ENVIADO → ENTREGADO`, con las variantes `LISTO_PARA_RETIRO`, `PAGO_RECHAZADO`, `EXPIRADO`, `CANCELADO` y `DEVUELTO`. El enum `EstadoPedido` define `puedePasarA(EstadoPedido)` y quien viola la transición lanza `TransicionEstadoInvalidaException`.

## 6. Reglas para `AGENTS.md`

1. **Dependencias:** van en el sentido `orquestador → modulos → shared`. `shared` no importa nada de `modulos` y ningún módulo importa al orquestador.
2. **Entre módulos:** solo por el `Service` público y por ID. Nunca se usa el `Repository` ni la `Entity` de otro módulo, ni `@ManyToOne` cruzado. `Pedido` guarda `usuarioId`, `productoId` y un snapshot de nombre y precio. Dentro de un mismo módulo sí se pueden relacionar entidades (Comuna → Region).
3. **Service (5):** clase concreta `@Service`, sin interfaz ni `Impl`. Convierte entity ↔ DTO con métodos privados (`aResponse`, `aEntidad`); no hay mapper. El Controller nunca ve una entity.
4. **DTO:** `record` con Bean Validation, en el `dto/` de su entidad (`XRequest` / `XResponse`).
5. **Estados (6):** `enum` con `@Enumerated(STRING)`. El enum define las transiciones válidas (`puedePasarA`) y quien las viola lanza `TransicionEstadoInvalidaException`.
6. **Excepciones (3)(4):** cada entidad lanza las suyas, extendiendo una base de `shared/exception`. Solo existe `GlobalExceptionHandler`; no hay handlers por módulo.
7. **Orquestador (7):** si un caso de uso necesita más de 3 services, va en `orquestador/`. No tiene repositories ni reglas de negocio propias; solo coordina services dentro de una `@Transactional`.
8. **Pago (8):** sin Strategy ni interfaz de pasarela. `PagoService` obtiene el resultado de la configuración del servidor, guarda la `TransaccionPago` y publica el evento. El cliente nunca define el resultado (RNF-SEG-10).
9. **Búsqueda (9):** un `@Query` con parámetros opcionales en el repository; en el MVP se usan categoría y rango de precio, y se pueden agregar más parámetros sin Specification.
10. **Observer (10):** `ApplicationEventPublisher` + `@EventListener` síncrono, en la misma transacción: si un listener falla, se revierte el pago completo. Los eventos son records en `shared/event`; los listeners viven en `listener/` de la entidad que reacciona.
11. **Idempotencia (11):** header `Idempotency-Key` obligatorio en `/api/v1/checkout` y `/api/v1/pagos/simular`; si falta, responde 400. Si `(clave, endpoint)` ya existe, devuelve `referenciaId` sin reejecutar. Si no existe, ejecuta y registra en la misma transacción. La PK compuesta evita la carrera (la segunda petición falla con 409).
12. **Docker (12)(13)(14):** tres contenedores (`db`, `backend`, `frontend`) en red interna. La BD no publica puerto y usa volumen. El backend usa `depends_on: condition: service_healthy` con `healthcheck` en la BD. La configuración sensible llega por variables de entorno.
13. **Flyway (15):** todo cambio de esquema va en `db/migration/Vn__descripcion.sql`; `ddl-auto=validate`. No se usan scripts sueltos ni `ddl-auto=update`.
14. **API (16):** todos los endpoints cuelgan de `/api/v1`.
15. **Seguridad (17):** JWT con access token puro, sin refresh ni tabla de revocación. `logout` es del cliente; el token sigue válido hasta expirar. El rol se obtiene del claim y se valida en el servidor.
16. **Rol:** el enum `shared/security/Rol` define autoridades; la tabla `ROL` se mapea con `RolEntidad` y se siembra con IDs fijos.

## 7. Supuestos tomados

- **Alcance:** se implementan las 22 tablas de `modeloNegocio.sql`, incluidas venta, boleta, blog, historial, movimiento de stock, carrito, oferta e imágenes.
- **Carrito:** se implementa módulo en servidor; el frontend puede seguir usando `localStorage` y sincronizar/validar contra el backend.
- **Filtros:** en el MVP, categoría y rango de precio; el mismo `@Query` admite más parámetros opcionales.
- **DTO:** el DTO vive junto a su entidad y la conversión ocurre dentro del Service.
- **Compartidos:** Config, Security, Exception, Event, Idempotency y DTO comunes están en `shared` para evitar ciclos.
- **Admin:** no hay módulo admin; los mantenedores son los mismos controllers protegidos por rol. El dashboard queda fuera del MVP.
- **Base de datos:** MySQL 8.4; migraciones Flyway.
- **API:** `/api/v1`.
- **Frontend:** tercer contenedor (React + Nginx); su build puede venir de un repo o carpeta hermana.
- **Idempotencia:** `CLAVE_IDEMPOTENCIA` es una tabla técnica, no una entidad de negocio del modelo.
- **Recuperación de contraseña:** no hay servicio de correo; `POST /api/v1/recuperacion/solicitar` devuelve el token en la respuesta para permitir la demostración. En producción se enviaría por correo y no se expondría.
- **Refresh token:** fuera del MVP; se evalúa en una fase posterior.
- **OpenAPI:** fuera del MVP; se incorpora en una fase posterior.
