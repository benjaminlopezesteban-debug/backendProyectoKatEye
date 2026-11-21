# Plan de desarrollo — Backend KatEye

| | |
|---|---|
| **Versión** | 1.1 — MVP conciliado con `ARQUITECTURA_BACKEND.md` |
| **Fecha** | 16 de noviembre de 2026 |
| **Estado** | Aprobado para iniciar implementación |
| **Proyecto** | KatEye — tienda virtual de cosméticos |
| **Documentos de referencia** | `requirements.md`, `architecture-assessment-fronend.md`, `ARQUITECTURA_BACKEND.md`, `modeloNegocio.sql` |

---

## 1. Contexto

El frontend React ya existe y simula la base de datos en archivos JavaScript. El backend reemplaza esa capa y pasa a ser la **fuente de verdad** de precios, stock, totales, estados de pedido y permisos, según el ERS v2.

El modelo de datos de referencia está en `modeloNegocio.sql` (22 tablas, Oracle, normalizado a 3FN). La arquitectura detallada, reglas de capas y estructura de paquetes viven en `ARQUITECTURA_BACKEND.md`; este documento resume el plan de ejecución.

---

## 2. Decisiones de arquitectura

| Tema | Decisión |
|---|---|
| Tipo de aplicación | **Monolito modular** (un solo módulo Spring Boot) |
| Agrupación | Entidades separadas por **dominio/funcionalidad** |
| Alcance | **Modelo completo: 22 tablas** de `modeloNegocio.sql` |
| Gateway | **No se implementa**; CORS y JWT viven en el monolito |
| Framework | Spring Boot **4.1.2**, Java 21 |
| Paquete base | `com.example.katEye` |
| Estructura | Reglas de `ARQUITECTURA_BACKEND.md`: `shared` + `modulos` + `orquestador` |
| Capa de servicio | Sin `Impl` ni mapper; DTO `record` y conversión privada en el `@Service` |
| Base de datos | **Una base MySQL** (`kateye`) |
| Migraciones | **Flyway** + semilla; `ddl-auto=validate` |
| Contenedores | **3**: frontend, backend y base de datos |
| Seguridad | **JWT con access token puro** (sin refresh token) |
| Estados del pedido | **10 estados del ERS §3.4** |
| Patrón de pago | **Sin Strategy**; simulación decidida por el servidor |
| Filtros de producto | `@Query` con parámetros opcionales; sin Specification |
| Comunicación interna | Services + eventos Spring (sin HTTP interno) |
| Documentación API | OpenAPI diferido a una fase posterior |
| Perfiles | Sin perfiles `dev`/`test`; una sola configuración |

---

## 3. Estructura del proyecto

```
KatEyeBackEnd/
├── AGENTS.md
├── README.md
├── docker-compose.yml
├── .env.example
├── .dockerignore
├── frontend/                        # contenedor React + Nginx
├── katEye/                          # módulo Spring Boot
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/example/katEye/
│       ├── KatEyeApplication.java
│       ├── shared/{config,security,exception,event,idempotency,dto}
│       ├── modulos/{ubicacion,usuarios,catalogo,carrito,pedidos,pagos,facturacion,contenido}
│       └── orquestador/checkout
└── docs/proyecto/
```

Cada entidad principal conserva `controller/ service/ repository/ entity/ dto/ exception/`; las entidades hijas solo `entity/ dto/`. Las reglas completas están en `ARQUITECTURA_BACKEND.md`.

---

## 4. Responsabilidad por dominio

| Dominio | Tablas | Responsabilidad |
|---|---|---|
| `ubicacion` | Region, Comuna | Datos de referencia; validar comuna ↔ región |
| `usuarios` | Usuario, Rol, Recuperacion, IntentoAcceso | Registro, login/logout, roles, recuperación, rate limit |
| `catalogo` | Categoria, Producto, ProductoImagen, Oferta, MovimientoStock | Catálogo, ofertas, stock e imágenes |
| `carrito` | Carrito, CarritoItem | Carrito validado y persistido |
| `pedidos` | Pedido, PedidoProducto, HistorialEstadoPedido | Pedidos, estados, snapshots e historial |
| `pagos` | TransaccionPago | Simulación de pago e idempotencia |
| `facturacion` | Venta, Boleta, DetalleBoleta | Consolidación contable y documento |
| `contenido` | EntradasBlog, MensajeContacto | Blog y formulario de contacto |
| `shared` | ClaveIdempotencia (técnica) | Idempotencia, seguridad, errores, eventos |
| `orquestador` | — | Checkout: coordina más de 3 services |

---

## 5. Reglas y patrones

Se adoptan las reglas de `ARQUITECTURA_BACKEND.md`:

- `orquestador → modulos → shared`; `shared` no importa módulos.
- Entre módulos solo se usa el `Service` público y el ID.
- `@Service` concreto, sin interfaz ni `Impl`; conversión entity ↔ DTO con métodos privados.
- DTO `record` con Bean Validation.
- Estados con `enum` y `@Enumerated(STRING)`; transiciones validadas por el enum.
- Excepciones propias por entidad sobre bases de `shared/exception`; un solo `GlobalExceptionHandler`.
- Casos de uso con más de 3 services van a `orquestador/`.
- Sin Strategy ni Specification.
- Observer con `ApplicationEventPublisher` + `@EventListener` síncrono en la misma transacción.
- Idempotencia con `ClaveIdempotencia` y header `Idempotency-Key`.

---

## 6. Dependencias

Un solo `pom.xml`. Se conserva `spring-boot-starter-parent 4.1.2` y Java 21. Se reemplaza H2 de runtime por MySQL.

| Dependencia | Uso |
|---|---|
| `spring-boot-starter-webmvc` | API REST |
| `spring-boot-starter-data-jpa` | Persistencia |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-security` | Autenticación y autorización |
| `spring-boot-starter-actuator` | Salud y métricas |
| `com.mysql:mysql-connector-j` | Driver |
| `spring-boot-starter-flyway` + `flyway-mysql` | Migraciones (Spring Boot 4 modulariza la autoconfiguración) |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | JWT |
| `spring-boot-starter-test`, `spring-security-test` | Pruebas |
| `spring-boot-testcontainers`, `testcontainers-mysql` | Integración con MySQL real (se agregan en fase 7) |

No se incluyen: gateway, OpenFeign, circuit breaker, Eureka, Spring Cloud, MapStruct, Lombok ni springdoc en esta etapa.

---

## 7. Seguridad — JWT con access token puro

- Un único `SecurityFilterChain`; `JwtService` firma y valida; `JwtAuthFilter` extrae usuario y roles de los claims sin consultar la BD.
- Roles: `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE` (authorities `ROLE_*`).
- Autorización con `@PreAuthorize`; ocultar un botón en el frontend no reemplaza la verificación.
- Contraseñas con BCrypt; el hash nunca se devuelve.
- Expiración configurable (`jwt.expiration`), recomendada: 60 minutos.
- **Rutas públicas:** catálogo, ofertas, blogs, regiones/comunas, contacto, registro, login y recuperación.
- **Rutas protegidas:** todo lo demás, con roles según la matriz del ERS.
- **Limitación del MVP:** sin refresh ni lista de revocación, `logout` es efectivo solo en el cliente. Se asume como decisión del MVP.

---

## 8. Persistencia

- Un contenedor MySQL 8.4, una base `kateye`.
- Flyway administra el esquema; `spring.jpa.hibernate.ddl-auto=validate`.
- Migraciones:
  - `V1__esquema.sql`: 22 tablas de negocio + `CLAVE_IDEMPOTENCIA` (técnica).
  - `V2__seed_ubicacion.sql`: 16 regiones y 346 comunas.
  - `V3__seed_admin.sql`: roles y administrador inicial.
  - `V4__seed_catalogo.sql`: categorías y productos.
  - `V5__contenido_fechas.sql`: `entradas_blog.publicada_en` y `mensaje_contacto.creado_en`.
  - `V6__seed_contenido.sql`: dos entradas de blog publicadas de demostración.
- `modeloNegocio.sql` (Oracle) es el modelo lógico; las migraciones son su equivalente MySQL (`BIGINT AUTO_INCREMENT`, `BOOLEAN`, `DATETIME(6)`, `JSON`).
- La base no publica puerto; solo es accesible por la red interna de Compose.

---

## 9. Docker

| Contenedor | Imagen / build | Puerto | Notas |
|---|---|---|---|
| `kateye-db` | `mysql:8.4` | — (solo red interna) | Volumen persistente y `healthcheck` |
| `kateye-backend` | `./katEye` multi-stage Maven → JRE 21 | 8080 | `depends_on: db: service_healthy` |
| `kateye-frontend` | `./frontend` React + Nginx | 80 | Proxy `/api` → backend |

Red interna `kateye-net`. Variables en `.env` (`MYSQL_*`, `JWT_SECRET`, `CORS_ORIGEN`, `PAGO_SIMULADO`).

```yaml
services:
  db:
    image: mysql:8.4
    environment:
      MYSQL_DATABASE: kateye
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - kateye_mysqldata:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -u$$MYSQL_USER -p$$MYSQL_PASSWORD --silent"]
      interval: 5s
      timeout: 5s
      retries: 10
    networks: [kateye-net]

  backend:
    build: ./katEye
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/kateye?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      KATEYE_PAGO_SIMULADO_RESULTADO: ${PAGO_SIMULADO:-APROBADA}
    depends_on:
      db:
        condition: service_healthy
    ports: ["8080:8080"]
    networks: [kateye-net]

  frontend:
    build: ./frontend
    depends_on: [backend]
    ports: ["80:80"]
    networks: [kateye-net]

volumes:
  kateye_mysqldata:

networks:
  kateye-net:
```

---

## 10. Contratos REST

Base: `/api/v1`. Un controlador por recurso dentro de su módulo.

### 10.1 Ubicación

| Método Java | HTTP | Ruta |
|---|---|---|
| `listarRegiones` | GET | `/regiones` |
| `obtenerRegion` | GET | `/regiones/{codigo}` |
| `listarComunasPorRegion` | GET | `/regiones/{codigo}/comunas` |
| `listarComunas` | GET | `/comunas` |
| `obtenerComuna` | GET | `/comunas/{codigo}` |

### 10.2 Usuarios

| Método Java | HTTP | Ruta |
|---|---|---|
| `registrar` | POST | `/auth/registro` |
| `login` | POST | `/auth/login` |
| `logout` | POST | `/auth/logout` |
| `obtenerPerfil` | GET | `/usuarios/me` |
| `listarUsuarios` | GET | `/usuarios` |
| `obtenerUsuario` | GET | `/usuarios/{id}` |
| `crearUsuario` | POST | `/usuarios` |
| `asignarRol` | PATCH | `/usuarios/{id}/rol` |
| `listarRoles` | GET | `/roles` |
| `solicitarRecuperacion` | POST | `/recuperacion/solicitar` |
| `validarTokenRecuperacion` | GET | `/recuperacion/validar` |
| `restablecerPassword` | POST | `/recuperacion/restablecer` |
| `listarIntentos` | GET | `/intentos-acceso` |

### 10.3 Catálogo

| Método Java | HTTP | Ruta |
|---|---|---|
| `listarProductos` | GET | `/productos` |
| `obtenerProducto` | GET | `/productos/{id}` |
| `obtenerRelacionados` | GET | `/productos/{id}/relacionados` |
| `crearProducto` | POST | `/productos` |
| `actualizarProducto` | PUT | `/productos/{id}` |
| `eliminarProducto` | DELETE | `/productos/{id}` |
| `actualizarStock` | PATCH | `/productos/{id}/stock` |
| `listarStockCritico` | GET | `/productos/stock-critico` |
| `listarCategorias` | GET | `/categorias` |
| `crearCategoria` | POST | `/categorias` |
| `actualizarCategoria` | PUT | `/categorias/{id}` |
| `eliminarCategoria` | DELETE | `/categorias/{id}` |
| `listarOfertasVigentes` | GET | `/ofertas` |
| `crearOferta` | POST | `/ofertas` |
| `actualizarOferta` | PUT | `/ofertas/{id}` |
| `eliminarOferta` | DELETE | `/ofertas/{id}` |
| `listarImagenes` | GET | `/productos/{id}/imagenes` |
| `subirImagen` | POST | `/productos/{id}/imagenes` |
| `eliminarImagen` | DELETE | `/productos/{id}/imagenes/{imagenId}` |
| `listarMovimientos` | GET | `/movimientos-stock` |

Funciones internas (sin endpoint): `ProductoService.obtenerPrecioVigente`, `verificarDisponibilidad`, `descontarStock`; `OfertaService.obtenerVigente`; `MovimientoStockService.reservar/liberar/confirmar`.

### 10.4 Carrito

| Método Java | HTTP | Ruta |
|---|---|---|
| `obtenerCarrito` / `crearCarrito` / `vaciarCarrito` | GET / POST / DELETE | `/carritos[/{id}]` |
| `validarCarrito` | POST | `/carritos/validar` |
| `listarItems` / `agregarItem` | GET / POST | `/carritos/{id}/items` |
| `actualizarCantidad` / `eliminarItem` | PUT / DELETE | `/carritos/{id}/items/{productoId}` |

El carrito se identifica por el `usuario_id` del JWT (no hay carrito anónimo). Todas las rutas exigen autenticación y cada operación valida que el carrito pertenezca al usuario de la sesión.

### 10.5 Pedidos

| Método Java | HTTP | Ruta |
|---|---|---|
| `confirmarCheckout` (orquestador) | POST | `/checkout` |
| `listarPedidos` | GET | `/pedidos` |
| `obtenerPedido` | GET | `/pedidos/{numero}` |
| `listarMisPedidos` | GET | `/pedidos/mios` |
| `consultarPedidoInvitado` | GET | `/pedidos/{numero}/invitado?correo=` |
| `cambiarEstado` | PATCH | `/pedidos/{numero}/estado` |
| `cancelarPedido` | PATCH | `/pedidos/{numero}/cancelar` |
| `registrarSeguimiento` | PATCH | `/pedidos/{numero}/seguimiento` |
| `listarLineas` | GET | `/pedidos/{id}/productos` |
| `listarHistorial` | GET | `/pedidos/{id}/historial` |

La creación del pedido ocurre **solo** por el orquestador de checkout, no por `POST /pedidos`. El número sigue el formato `yyyyMMdd-####` (correlativo diario, por ejemplo `20261117-0001`). La consulta de invitado exige el correo de contacto y solo aplica a pedidos sin usuario registrado.

### 10.6 Pagos

| Método Java | HTTP | Ruta |
|---|---|---|
| `simularPago` | POST | `/pagos/simular` |
| `obtenerPago` | GET | `/pagos/{id}` |
| `listarPagosPorPedido` | GET | `/pagos/pedido/{numero}` |
| `reintentarPago` | POST | `/pagos/{numero}/reintentar` |

El resultado de la simulación lo decide el servidor (`kateye.pago.simulado-resultado`). El request solo envía `pedidoId` y `metodo`.

### 10.7 Facturación

| Método Java | HTTP | Ruta |
|---|---|---|
| `listarVentas` / `obtenerVenta` | GET | `/ventas[/{id}]` |
| `obtenerVentaPorPedido` | GET | `/ventas/pedido/{pedidoId}` |
| `anularVenta` | PATCH | `/ventas/{id}/anular` |
| `listarBoletas` / `obtenerBoleta` | GET | `/boletas[/{folio}]` |
| `emitirBoleta` | POST | `/boletas` |
| `anularBoleta` | PATCH | `/boletas/{folio}/anular` |
| `listarDetalleBoleta` | GET | `/boletas/{id}/detalle` |

### 10.8 Contenido

| Método Java | HTTP | Ruta |
|---|---|---|
| `listarEntradas` | GET | `/blogs` |
| `obtenerPorSlug` | GET | `/blogs/{slug}` |
| `crearEntrada` | POST | `/blogs` |
| `actualizarEntrada` | PUT | `/blogs/{id}` |
| `publicarEntrada` / `despublicarEntrada` | PATCH | `/blogs/{id}/publicacion` |
| `enviarMensaje` | POST | `/contacto` |
| `listarMensajes` / `obtenerMensaje` | GET | `/contacto[/{id}]` |
| `marcarLeido` / `marcarRespondido` | PATCH | `/contacto/{id}/estado` |

**Blog.** La lista pública (`GET /blogs`) solo devuelve entradas publicadas, ordenadas por `publicadaEn` descendente. Un administrador autenticado ve todas y puede filtrar con `?publicada=true|false`. El detalle público de un borrador responde 404. Crear, actualizar y publicar exige `ADMINISTRADOR`.

El request usa el mismo contrato que el frontend existente:

```json
{
  "titulo": "Rutina de skincare",
  "slug": "rutina-de-skincare",
  "resumen": "Tres pasos simples",
  "imagen": "Crema Hidratante Facial.jpg",
  "contenido": [
    {"tipo": "titulo", "texto": "Manana"},
    {"tipo": "parrafo", "texto": "Texto del parrafo"},
    {"tipo": "lista", "items": [{"etiqueta": "Limpieza", "texto": "Usa un limpiador suave"}]}
  ]
}
```

`slug` es opcional: si falta o viene vacío se deriva del título (minúsculas, sin acentos, separado por guiones). Un slug duplicado responde 409. `imagenUrl` y `bloques` se aceptan como alias de `imagen` y `contenido` por compatibilidad. Los bloques se guardan como `JSON` y se validan: `titulo`/`parrafo` requieren `texto`, `lista` requiere al menos un ítem y cualquier otro tipo responde 400.

**Contacto.** `POST /contacto` es público y exige `nombre`, `correo` y `comentario`; `contenido` se acepta como alias de `comentario`. Listar, consultar y cambiar estado exigen `ADMINISTRADOR`. Transiciones permitidas: `NUEVO → LEIDO|RESPONDIDO` y `LEIDO → RESPONDIDO`; repetir el estado actual no modifica nada y una transición inválida responde 422. Al pasar a `RESPONDIDO` se registra `respondidoPor`; `creadoEn` se asigna al recibir el mensaje.

---

## 11. Comunicación interna

Sin HTTP interno. Se usan services inyectados y eventos de Spring:

| Servicio | Funciones usadas por otros módulos |
|---|---|
| `ProductoService` | `obtenerEntidad`, `obtenerPrecioVigente`, `verificarDisponibilidad`, `descontarStock` |
| `OfertaService` | `obtenerVigentePorProducto` |
| `ComunaService` | `obtener`, `validarPertenenciaRegion` |
| `UsuarioService` | `obtenerEntidad`, `obtenerPorCorreo` |
| `CarritoService` | `itemsDe`, `itemsMios`, `vaciar` |
| `PedidoService` | `crear`, `obtenerEntidad`, `obtenerResponsePorId`, `cambiarEstado` |
| `PagoService` | `simular`, `obtener` |
| `MovimientoStockService` | `reservar`, `liberar`, `confirmar` |
| `VentaService` | `crearDesdePedido` |
| `BoletaService` | `emitir` |
| `HistorialEstadoService` | `registrar` |
| `RecuperacionService` | `crearToken`, `consumirToken` |
| `IntentoAccesoService` | `registrar`, `contarRecientes` |

### Flujo de checkout

1. `IdempotenciaService.buscar`.
2. `CarritoService` (items del carrito propio) o `ProductoService.verificarDisponibilidad` por ítem.
3. `UsuarioService.obtener` si hay sesión para autocompletar contacto; `ComunaService.validarPertenenciaRegion`.
4. `PedidoService.crear` en `PENDIENTE_PAGO` con snapshots y totales calculados en servidor; `HistorialEstadoService.registrar`.
5. `MovimientoStockService.reservar` por ítem con lock pesimista; la reserva descuenta stock de inmediato.
6. Vaciar el carrito si el checkout usó sus ítems.
7. `IdempotenciaService.registrar`.
8. Devuelve número, total y estado.

La reserva descuenta `producto.stock` al confirmar el checkout. `liberar` repone el stock y registra `LIBERACION`; `confirmar` (al aprobarse el pago) solo registra `SALIDA`, sin volver a descontar.

### Flujo de pago

`PagoService.simular` obtiene el pedido, exige `PENDIENTE_PAGO`, decide el resultado por configuración y guarda `TransaccionPago`.

- **Aprobado:** `PagoAprobadoEvent` → pedido a `PAGADO`, confirma la reserva (registra `SALIDA` sin descontar de nuevo), crea `Venta`, emite `Boleta` y registra historial.
- **Rechazado:** `PagoRechazadoEvent` → pedido a `PAGO_RECHAZADO`, libera reserva y registra historial.

Los listeners son síncronos y comparten la transacción; si uno falla, se revierte el pago completo.

---

## 12. OpenAPI

Fuera del MVP (`springdoc` no se incluye). Cuando se incorpore, permitirá generar el contrato OpenAPI 3, probar endpoints con Swagger UI, alinear frontend/backend y documentar la entrega. Se activará en una fase posterior.

---

## 13. Fases de implementación

| Fase | Entregable |
|---|---|
| 0 | `pom.xml`, `application.yml`, Docker Compose (3 contenedores), Flyway `V1` con 22 tablas + idempotencia, `shared` |
| 1 | `ubicacion` + seed geográfico |
| 2 | `usuarios` (JWT, roles, recuperación, intentos) + seed admin |
| 3 | `catalogo` (productos, categorías, ofertas, imágenes, stock) + seed catálogo |
| 4 | `carrito` + `pedidos` + `orquestador/checkout` |
| 5 | `pagos` (simulación en servidor) + `facturacion` (venta, boleta) |
| 6 | `contenido` (blog, contacto) |
| 7 | Pruebas, hardening y documentación |

---

## 14. Decisiones pendientes y futuras

| Tema | Estado |
|---|---|
| Refresh token y revocación de sesión | Fuera del MVP; se evalúa en una fase posterior |
| OpenAPI / Swagger UI | Fuera del MVP; fase posterior |
| Proveedor de pago | A definir (DP-03); el MVP usa simulación |
| Almacenamiento de imágenes | Carpeta local detrás de una interfaz (DP-09) |
| Cobertura de despacho y tarifas | A definir (DP-06) |
| Perfiles `dev`/`test` | No configurados por ahora; una sola configuración |

---

## 15. Referencias

- `requirements.md` — Especificación de Requerimientos de Software v2.0.
- `architecture-assessment-fronend.md` — Arquitectura técnica del frontend React.
- `ARQUITECTURA_BACKEND.md` — Arquitectura detallada, estructura de paquetes y reglas.
- `modeloNegocio.sql` — Modelo lógico normalizado a 3FN.
