# Especificación de Requerimientos de Software (ERS) — Tienda Virtual KatEye

| | |
|---|---|
| **Versión** | 2.0 — Backend (borrador para revisión del equipo) |
| **Fecha** | 7 de noviembre de 2026 |
| **Tipo** | Caso de presentación hipotético |
| **Documentos de origen** | `requirements.md` v1 (preliminar) y *Kateyes — Arquitectura técnica del frontend React* (octubre de 2026) |

> **Cómo leer este documento.** El frontend ya existe. Este ERS toma lo que el frontend *simula* y lo convierte en lo que el backend *debe garantizar*. Cada requerimiento indica su **Origen**:
> - **Existe**: el frontend ya lo hace y el backend lo reemplaza.
> - **Cambia**: el frontend lo hace de una forma que el backend no debe copiar.
> - **Nuevo**: el frontend no lo tiene.
>
> Si el frontend y este ERS discrepan, **prevalece el ERS** y se corrige el frontend (ver sección 2.4).

---

## 1. Introducción

### 1.1 Contexto

KatEye es una pequeña empresa que hasta ahora sostiene su negocio mostrando sus productos en distintas redes sociales y gestionando los pagos, las transferencias y el envío mediante los chats de cada una de ellas. El problema principal es la **fragmentación de los canales** con los que se atiende a los clientes: la operación se vuelve engorrosa y propensa a errores.

A petición de la empresa se desarrollará una página web que derive a los clientes desde las redes sociales hacia una plataforma segura de pago y gestión del envío, separando así las responsabilidades de marketing (redes sociales) de las de ventas (sitio web).

Además, el sitio debe servir como plataforma del negocio: presentar los productos, gestionar usuarios (clientes registrados y personal administrativo), publicar información de productos en blogs y ofrecer otras secciones informativas.

### 1.2 Propósito de este documento

Definir qué debe hacer el backend de la tienda, partiendo del frontend ya construido. El frontend se trata como **prototipo y fuente de evidencia**, no como especificación: lo que hoy hace en el navegador (precios, totales, pagos, sesión) pasa a ser responsabilidad del servidor.

### 1.3 Objetivos

| ID | Objetivo |
|---|---|
| O-1 | Centralizar el proceso de compra (catálogo, carrito, pedido, pago, despacho o retiro) en un único canal. |
| O-2 | Hacer del backend la **fuente de verdad** de precios, stock, totales, estados de pedidos y permisos. |
| O-3 | Dejar una base que pueda escalar mediante servicios independientes. |
| O-4 | Proteger los datos personales de los clientes y no manipular datos de tarjetas. |

### 1.4 Alcance

| Dentro de la versión 1 | Fuera de la versión 1 |
|---|---|
| Catálogo: productos, categorías, ofertas, stock e imágenes | Emisión de boleta o factura electrónica (SII) |
| Usuarios, roles y autenticación | Cálculo de tarifas de courier en tiempo real |
| Validación del carrito y pedidos con estados | Favoritos, reseñas y valoraciones, selector de tono (no están en el frontend actual) |
| Pago mediante un proveedor externo (ambiente de pruebas) | Cupones, programa de puntos, multimoneda |
| Despacho o retiro, con datos y cobertura básicos | Aplicación móvil |
| Blogs y formulario de contacto | Cobro real en producción |
| Administración: productos, usuarios, pedidos y panel de resumen | |

### 1.5 Actores y roles

| Actor | Descripción |
|---|---|
| **Visitante** | Sin sesión. Navega, contacta y puede comprar como invitado. |
| **Cliente** | Usuario registrado. Sus datos se precargan y consulta su historial de pedidos. |
| **Vendedor** | Gestiona productos y pedidos. No gestiona usuarios. |
| **Administrador** | Acceso total, incluidos usuarios y roles. |
| **Proveedor de pagos** | Sistema externo que informa el resultado de cada pago. |

> La versión 1 de este documento hablaba solo de "clientes registrados y administradores". Se adoptan tres roles (Administrador, Vendedor, Cliente), que son los que definen las validaciones del panel de administración.

### 1.6 Convenciones

- **Identificadores:** `RF` funcional, `RNF` no funcional, `RN` regla de negocio, `DP` decisión pendiente.
- **Prioridad (MoSCoW):** **M** imprescindible, **S** importante, **C** deseable. La prioridad la indica la columna; en el texto siempre se usa "deberá".
- **Moneda:** pesos chilenos (CLP), valores enteros.

---

## 2. Solución

### 2.1 Visión general

Se desarrollará una aplicación web que integra tres dinámicas:

1. **Tienda:** presenta los productos con su información detallada, permite filtrarlos por categoría, muestra precios de oferta, productos nuevos y blogs informativos. Los productos se seleccionan mediante un carrito.
2. **Pedidos y pago:** registra el pedido con los datos del cliente y la entrega (despacho o retiro), gestiona los **estados de la compra** y se integra con un **proveedor de pagos externo**.
3. **Envío:** en esta etapa solo lo básico: solicitar el lugar de despacho y validar los datos.

> **Corrección respecto de la v1:** la aplicación **no construye una pasarela de pago**; las pasarelas (Webpay Plus, Flow, Mercado Pago, etc.) son servicios de terceros. Lo que KatEye implementa es el **módulo de pedidos y pagos** que se integra con una de ellas (ver DP-03).

El frontend tentativo fue presentado a los administradores y quedaron conformes con la solución preliminar, pero esperan que la aplicación pueda mejorarse y escalar.

### 2.2 Punto de partida: el frontend actual

| Tema | Estado actual |
|---|---|
| Tecnología | SPA con React y Vite, React Router, Bootstrap 5.3 |
| Datos | Archivos JS que simulan la base de datos: `productos.js`, `blogs.js`, `regiones.js`, `usuarios.js` |
| Estado compartido | Contextos de carrito y de sesión; persistencia en `localStorage` |
| Vistas | Inicio, Productos, Detalle de producto, Checkout (confirmación y pago fallido), Blogs, Blog, Nosotros, Contacto, Iniciar sesión, Registro |
| Pruebas | 165 pruebas con Vitest y Testing Library |
| No migrado | Panel de administración |

La arquitectura por capas del frontend (datos y reglas → utilidades → estado → componentes → vistas) es una ventaja: **la capa 1 es la única que debe reemplazarse** por clientes de la API. Los componentes y las vistas no deberían cambiar de forma importante, salvo por los estados de carga y error (sección 5).

### 2.3 Arquitectura propuesta (restricción de diseño)

La decisión de usar **Spring Boot con microservicios** es una restricción de diseño del equipo, no un requerimiento de negocio, por eso se documenta aquí y no en la introducción.

| Servicio | Responsabilidad | Reemplaza en el frontend | Datos propios |
|---|---|---|---|
| `api-gateway` | Punto de entrada único: enrutamiento, CORS, verificación del token | — | — |
| `catalogo-service` | Productos, categorías, ofertas, stock y reservas, imágenes, blogs, regiones y comunas | `productos.js`, `blogs.js`, `regiones.js`, `utils/imagenes.js` | Producto, Categoría, ImagenProducto, ReservaStock, EntradaBlog, Región, Comuna |
| `usuarios-service` | Registro, autenticación, roles, perfil, recuperación de contraseña, administración de usuarios | `usuarios.js` y la parte de datos de `SesionProvider` | Usuario |
| `pedidos-service` | Validación del carrito, creación de pedidos, estados, entrega, historial | reglas de `carrito.js` y `pedidos.js` | Pedido, LíneaPedido, HistorialEstado |
| `pagos-service` | Integración con el proveedor, webhooks, transacciones | `pagos.js` | TransacciónPago |
| `notificaciones-service` | Correos transaccionales y mensajes de contacto | vista `Contacto` | MensajeContacto |
| `Docker-Fronted` | Conteneriza y despliega el frontend del proyecto | Ejecucion Manual de npm run dev | — |
| `Docker-BackEnd` | Conteneriza dependencias y configuración y despliega el backend del proyecto | Ejecucion Manual de ./mvnw spring-boot:run    | — |
| `Docker-DataBase` | Conteneriza la base de datos del proyecto y configuración despliega| Guardado los datos en los archivos javascript | — |



**Principios entre servicios:**

- Cada servicio es dueño de su base de datos; ningún servicio lee tablas de otro. Un contenedor desplegara 
- Las consultas entre servicios usan REST (por ejemplo, `pedidos-service` consulta precio y stock a `catalogo-service`).
- Una caída de `notificaciones-service` no debe impedir crear ni pagar pedidos.
- Todo cambio de estado que involucre más de un servicio (pedido, stock, pago) debe poder **compensarse** si un paso falla (ver RNF-CON-01).
- Para un negocio de este tamaño, el mínimo viable son tres servicios (`catalogo`, `usuarios`, `pedidos` con pagos incluidos); ver DP-14.

### 2.4 Reglas de relación entre frontend y backend

Construir el frontend primero es una forma válida de descubrir requerimientos; el riesgo aparece cuando el prototipo se convierte en la especificación. Para evitarlo:

1. **El backend decide, el frontend muestra.** Precios, descuentos, totales, stock, número de pedido, estado de pago y permisos se calculan siempre en el servidor. Lo que hoy calculan `carrito.js`, `pedidos.js` y `pagos.js` pasa a ser una *estimación visual*.
2. **Las validaciones se definen una sola vez**, en la sección 3.3. El frontend valida para mejorar la experiencia; el backend valida siempre.
3. **Contrato primero.** Se escribe la especificación OpenAPI antes del código de cada servicio. El panel de administración (aún no migrado) se construye sobre ese contrato, en el orden correcto.
4. **Cambios aditivos.** Mientras sea posible, la API conserva los nombres de campo que hoy usa el frontend (`id`, `nombre`, `precio`, `precioAnterior`, `categoria`, `imagenes`, `descripcion`) y agrega los nuevos sin renombrar los existentes.
5. **Conflictos:** si el frontend contradice este ERS, se corrige el frontend.

---

## 3. Requerimientos

### 3.1 Requerimientos funcionales

#### 3.1.1 Catálogo

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-CAT-01 | El sistema deberá listar productos con filtros por una o más categorías, rango de precio, texto, `enOferta` y `nuevo`, con orden y paginación. El filtrado deja de hacerse en el navegador. | M | Cambia | `productos.js`, `Productos`, `Inicio` |
| RF-CAT-02 | El sistema deberá entregar el detalle de un producto: nombre, descripción, categoría, precio, precio anterior vigente, imágenes, disponibilidad y máximo comprable. Si no existe, responderá 404 y la vista mostrará "Producto no encontrado". | M | Existe | `obtenerProductoPorId`, `DetalleProducto` |
| RF-CAT-03 | El sistema deberá entregar productos relacionados: los de la misma categoría, excluyendo el actual, con un máximo configurable. | S | Existe | `DetalleProducto`, `TarjetaRelacionada` |
| RF-CAT-04 | El sistema deberá gestionar las categorías como entidad propia (id, slug, nombre, descripción corta, orden). El menú del Header y el filtro se alimentan de ese listado; el parámetro de la URL usa el *slug*, no el nombre con tildes. | M | Cambia | `obtenerCategorias`, `Header`, `?categoria=` |
| RF-CAT-05 | El sistema deberá gestionar ofertas: precio anterior con vigencia (desde y hasta). El porcentaje de descuento lo calcula el servidor y una oferta vencida deja de mostrarse sola. Habrá un listado propio de ofertas. | M | Nuevo | `Precio`, `precioAnterior` |
| RF-CAT-06 | El sistema deberá marcar como "nuevos" los productos creados dentro de una ventana configurable (por defecto, 30 días). | S | Nuevo | La v1 los menciona; el frontend no los muestra |
| RF-CAT-07 | El sistema deberá registrar stock y stock crítico por producto, y generar una alerta cuando el stock llegue al crítico. | M | Nuevo | — |
| RF-CAT-08 | El sistema deberá almacenar imágenes (JPG, PNG o WebP, hasta 2 MB), con una o más por producto, y entregarlas por URL pública. Reemplaza los archivos de `src/assets` resueltos con `import.meta.glob`. | M | Cambia | `utils/imagenes.js` |
| RF-CAT-09 | El sistema deberá permitir a Administrador y Vendedor crear, actualizar y dar de baja productos. La baja es lógica: un producto con pedidos asociados no se borra. | M | Existe | `crearProducto`, `actualizarProducto`, `eliminarProducto` |
| RF-CAT-10 | El sistema deberá asignar a cada producto un código único (mínimo 3 caracteres). | M | Nuevo | — |

#### 3.1.2 Carrito

Decisión inicial (DP-04): el carrito **sigue viviendo en el navegador** (`kateye_carrito`, `[{ id, cantidad }]`) y el backend lo **valida**; no lo almacena.

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-CAR-01 | El sistema deberá recibir una lista `[{ productoId, cantidad }]` y devolver cada línea con nombre, precio vigente, precio anterior, cantidad aceptada, subtotal, ahorro y total, junto con advertencias (producto inexistente, sin stock, cantidad ajustada, precio cambiado). | M | Nuevo | `detallarCarrito`, `PanelCarrito`, `Checkout` |
| RF-CAR-02 | El sistema deberá aplicar las reglas del carrito (RN-02, RN-03) y no confiar en nombres, precios ni totales enviados por el cliente. | M | Cambia | `carrito.js` |
| RF-CAR-03 | Si un precio cambió entre que el cliente agregó el producto y que llegó al checkout, el sistema deberá informarlo para que el cliente lo confirme antes de pagar. | S | Nuevo | `Checkout` |
| RF-CAR-04 | El sistema podría guardar el carrito de usuarios autenticados y fusionarlo con el local al iniciar sesión. | C | Nuevo | `CarritoProvider` |

#### 3.1.3 Usuarios y autenticación

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-AUT-01 | El sistema deberá registrar clientes con correo, contraseña, nombre, teléfono (opcional), región y comuna. El correo es único sin distinguir mayúsculas. | M | Cambia | `registrarUsuario`, `Registro` |
| RF-AUT-02 | El sistema deberá autenticar con correo y contraseña y entregar un token de acceso de corta duración y un token de renovación. Ante credenciales inválidas responderá siempre el mismo mensaje genérico. | M | Cambia | `autenticar`, `IniciarSesion` |
| RF-AUT-03 | El sistema deberá permitir cerrar sesión invalidando el token de renovación. | M | Cambia | `cerrarSesion` |
| RF-AUT-04 | El sistema deberá entregar los datos del usuario autenticado (`/usuarios/me`) para precargar el checkout y el saludo del Header. Nunca devolverá la contraseña ni su hash. | M | Cambia | `SesionProvider`, `Checkout` |
| RF-AUT-05 | El sistema deberá permitir al cliente actualizar su perfil (nombre, teléfono, región, comuna, dirección). | S | Nuevo | — |
| RF-AUT-06 | El sistema deberá permitir recuperar la contraseña mediante un enlace de un solo uso enviado al correo. | S | Nuevo | — |
| RF-AUT-07 | El sistema deberá aplicar control de acceso por rol en cada endpoint (matriz 3.1.9). Ocultar un botón en el frontend no sustituye esta verificación. | M | Nuevo | — |
| RF-USR-01 | El sistema deberá permitir al Administrador listar usuarios con búsqueda por nombre, correo o región y paginación. | M | Nuevo | Panel admin |
| RF-USR-02 | El sistema deberá permitir al Administrador crear, editar, desactivar usuarios y asignar su rol. | M | Nuevo | Panel admin |

#### 3.1.4 Pedidos

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-PED-01 | El sistema deberá crear un pedido a partir de `[{ productoId, cantidad }]`, datos de contacto, entrega y método de pago. **Recalculará todos los montos** e ignorará los que envíe el cliente. El pedido nace en estado `PENDIENTE_PAGO` y reserva el stock. | M | Cambia | `crearPedido`, `Checkout` |
| RF-PED-02 | El sistema deberá asignar a cada pedido un número legible y único (por ejemplo `KE-000123`) generado por el servidor mediante una secuencia, no derivado de la hora del cliente. | M | Cambia | `pedidos.js` |
| RF-PED-03 | El sistema deberá aceptar compras de invitados (con datos de contacto) y de clientes autenticados (con sus datos precargados y el pedido vinculado a su cuenta). | M | Nuevo | `Checkout`, `useSesion` |
| RF-PED-04 | El sistema deberá registrar el tipo de entrega, **despacho o retiro**. El despacho exige dirección, comuna y región; el retiro no exige dirección. | M | Nuevo | `Checkout` no lo ofrece |
| RF-PED-05 | El sistema deberá guardar en cada línea una copia (*snapshot*) de nombre, precio unitario, precio anterior y cantidad, y en el pedido la dirección de entrega, para que cambios posteriores del catálogo no alteren pedidos pasados. | M | Existe | `kateye_ultimo_pedido` |
| RF-PED-06 | El sistema deberá gestionar los estados del pedido según la sección 3.4 y registrar en un historial cada cambio (quién, cuándo, motivo). | M | Nuevo | — |
| RF-PED-07 | El sistema deberá entregar al cliente su historial de pedidos y el detalle de cada uno. Reemplaza el almacenamiento del *último* pedido en `localStorage`. | M | Cambia | `kateye_ultimo_pedido` |
| RF-PED-08 | El sistema deberá permitir a un invitado consultar un pedido con número y correo. | S | Nuevo | — |
| RF-PED-09 | El sistema deberá enviar un correo de confirmación al pagar un pedido. La pantalla de confirmación ya promete este envío ("Enviaremos el detalle a…"). | M | Nuevo | `ConfirmacionPedido` |
| RF-PED-10 | El sistema deberá permitir a Vendedor y Administrador listar pedidos (filtro por estado y fecha), ver su detalle, avanzar su estado, registrar el código de seguimiento y cancelar con motivo. | M | Nuevo | Panel admin |
| RF-PED-11 | El sistema deberá permitir al cliente cancelar un pedido mientras esté en `PENDIENTE_PAGO`. | S | Nuevo | — |
| RF-PED-12 | El sistema deberá permitir registrar una solicitud de retracto dentro del plazo legal (ver RNF-LEG-02). | S | Nuevo | — |

#### 3.1.5 Pagos

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-PAG-01 | El sistema deberá iniciar el pago de un pedido con el proveedor externo y devolver al frontend los datos para continuar (por ejemplo, una URL de redirección). | M | Cambia | `procesarPago` |
| RF-PAG-02 | El sistema deberá recibir el resultado desde el proveedor (webhook o retorno), **verificar su firma** y actualizar el pedido. El resultado del pago nunca se toma de lo que informe el navegador. | M | Cambia | `pagos.js` |
| RF-PAG-03 | Ante un pago rechazado, el pedido deberá conservarse (`PAGO_RECHAZADO`), con motivo y código traducidos a un mensaje comprensible, y permitir reintentar sobre el mismo pedido mientras no expire. | M | Cambia | `PagoFallido` |
| RF-PAG-04 | El sistema deberá garantizar que un pedido pagado no pueda pagarse dos veces y que un mismo aviso del proveedor, recibido más de una vez, produzca un único efecto (idempotencia). | M | Nuevo | — |
| RF-PAG-05 | El sistema deberá soportar tarjeta de crédito o débito (resultado inmediato, vía proveedor) y transferencia bancaria (resultado diferido: el pedido queda en `PENDIENTE_PAGO` hasta que se confirme la recepción). | M | Cambia | `Checkout` trata ambas igual |
| RF-PAG-06 | El sistema deberá registrar cada transacción (monto, proveedor, referencia, código de respuesta, fecha) para conciliación. | S | Nuevo | — |

#### 3.1.6 Envío

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-ENV-01 | El sistema deberá validar que la comuna exista y pertenezca a la región indicada. | M | Cambia | `regiones.js` |
| RF-ENV-02 | El sistema deberá exponer las 16 regiones y 346 comunas (`GET /regiones`, `GET /regiones/{codigo}/comunas`), cargadas como datos de referencia. | M | Existe | `REGIONES`, `obtenerComunas` |
| RF-ENV-03 | El sistema deberá validar la cobertura de despacho por comuna (DP-06). | M | Nuevo | — |
| RF-ENV-04 | El sistema deberá calcular el costo de despacho según la regla que se defina y sumarlo al total (DP-06). | S | Nuevo | El resumen no lo muestra |
| RF-ENV-05 | El sistema deberá registrar empresa de transporte y código de seguimiento al pasar el pedido a `ENVIADO`, y mostrarlos al cliente. | S | Nuevo | — |

#### 3.1.7 Contenido y contacto

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-BLG-01 | El sistema deberá listar las entradas publicadas del blog (título, resumen, imagen, fecha). | M | Existe | `obtenerBlogs`, `Blogs` |
| RF-BLG-02 | El sistema deberá entregar una entrada por *slug*, con su contenido como bloques estructurados (título, párrafo, lista), no como HTML. | M | Existe | `obtenerBlogPorSlug`, `Blog` |
| RF-BLG-03 | El sistema deberá permitir al Administrador crear, editar, publicar y despublicar entradas. | S | Nuevo | — |
| RF-BLG-04 | El sistema podría vincular una entrada con uno o más productos. | C | Nuevo | La v1 pide "información de productos en blogs" |
| RF-CON-01 | El sistema deberá recibir y guardar los mensajes del formulario de contacto (nombre, correo, comentario de hasta 500 caracteres). | M | Cambia | `Contacto` |
| RF-CON-02 | El sistema deberá permitir al Administrador listar los mensajes y marcarlos como leídos o respondidos. | S | Nuevo | — |
| RF-CON-03 | El sistema podría notificar por correo al equipo cuando llegue un mensaje. | C | Nuevo | — |

#### 3.1.8 Administración

| ID | Requerimiento | Prio | Origen | Frontend relacionado |
|---|---|---|---|---|
| RF-ADM-01 | Todos los listados de administración (productos, usuarios, pedidos, mensajes) deberán ofrecer paginación, búsqueda y orden. | M | Nuevo | Panel admin (no migrado) |
| RF-ADM-02 | El sistema deberá entregar un resumen para el panel principal: usuarios, productos, ventas del período, pedidos por estado y productos con stock crítico. | S | Nuevo | Panel admin |
| RF-ADM-03 | El sistema deberá registrar una auditoría de cambios sensibles (precio, stock, estado de pedido, rol de usuario): quién, cuándo, valor anterior y nuevo. | S | Nuevo | — |

#### 3.1.9 Matriz de permisos

| Recurso | Visitante | Cliente | Vendedor | Administrador |
|---|---|---|---|---|
| Ver catálogo, ofertas, blogs, regiones | ✔ | ✔ | ✔ | ✔ |
| Validar carrito y crear pedido | ✔ | ✔ | — | — |
| Ver mis pedidos y mi perfil | — | ✔ (propios) | — | — |
| Enviar mensaje de contacto | ✔ | ✔ | ✔ | ✔ |
| Crear, editar y dar de baja productos y categorías | — | — | ✔ | ✔ |
| Ver y gestionar pedidos | — | — | ✔ | ✔ |
| Gestionar usuarios y roles | — | — | — | ✔ |
| Gestionar blogs y ver mensajes de contacto | — | — | — | ✔ |
| Ver resumen del panel y auditoría | — | — | — | ✔ |

### 3.2 Reglas de negocio

| ID | Regla |
|---|---|
| RN-01 | Los precios son enteros en CLP (sin decimales) e incluyen IVA (confirmar en DP-13). |
| RN-02 | La cantidad por producto en un pedido es un entero entre 1 y 10 y no puede superar el stock disponible. Si se supera, se limita y se avisa. |
| RN-03 | Un producto aparece una sola vez por pedido; agregarlo de nuevo suma a su línea. Una línea que llega a 0 se elimina. |
| RN-04 | El precio de una línea es el precio vigente del catálogo al crear el pedido. El precio anterior solo se considera si es mayor al precio actual y la oferta está vigente. |
| RN-05 | Descuento (%) = redondeo de (1 − precio / precio anterior) × 100. Total = suma de precio × cantidad + costo de despacho. Ahorro = suma de (precio anterior − precio) × cantidad, solo en ofertas vigentes. |
| RN-06 | El número de pedido lo genera el servidor y es único. |
| RN-07 | El stock se **reserva** al crear el pedido por un tiempo limitado (DP-08), se **confirma** al pagar y se **libera** si el pedido expira, se cancela o el pago se rechaza de forma definitiva. Dos compras simultáneas del último ítem deben resultar en una sola aprobada. |
| RN-08 | Un pedido pagado nunca se elimina; solo cambia de estado. |
| RN-09 | Un producto con pedidos asociados se da de baja de forma lógica. |
| RN-10 | El correo de un usuario es único sin distinguir mayúsculas. |
| RN-11 | Una oferta vencida deja de mostrarse y el producto vuelve a su precio normal sin intervención manual. |
| RN-12 | Un rol solo puede ser asignado por un Administrador. |

### 3.3 Reglas de validación de datos

Estas reglas se aplican en el **backend siempre**; el frontend las repite para dar retroalimentación inmediata. Los casos de prueba de `validaciones.test.js` y `usuarios.test.js` sirven de base para las pruebas del backend.

| Campo | Regla | Nota |
|---|---|---|
| Correo | Obligatorio (opcional en contacto), máximo 100 caracteres, formato válido, dominio `duoc.cl`, `profesor.duoc.cl` o `gmail.com` | El dominio es una restricción del ejercicio académico (DP-01) |
| Contraseña | De 4 a 10 caracteres | Restricción académica; se recomienda de 8 a 64 (DP-02). Se guarda solo con hash |
| Confirmar contraseña | Debe coincidir con la contraseña | Validación solo de interfaz; el backend no la recibe |
| Nombre completo | Obligatorio, máximo 100 caracteres | El panel admin pide nombre (50) y apellidos (100) por separado (DP-05) |
| RUN | De 7 a 9 caracteres, sin puntos ni guion, con dígito verificador válido | Solo donde se pida (DP-05) |
| Teléfono | Registro: opcional. Checkout: obligatorio. Celular chileno, normalizado a `+569XXXXXXXX` | La expresión actual del frontend acepta espacios y cantidades de dígitos poco estrictas |
| Región y comuna | Obligatorias en registro y despacho; la comuna debe pertenecer a la región | |
| Dirección | Obligatoria en despacho, máximo 300 caracteres | Se unifica con el panel admin (la versión HTML del checkout permitía 150) |
| Comentario de contacto | Obligatorio, máximo 500 caracteres | |
| Código de producto | Obligatorio, mínimo 3 caracteres, único | |
| Nombre de producto | Obligatorio, máximo 100 caracteres | |
| Descripción | Máximo 500 caracteres | |
| Precio | Entero mayor o igual a 0 | El panel admin permitía decimales; en CLP no corresponde |
| Stock | Entero mayor o igual a 0 | |
| Stock crítico | Opcional, entero mayor o igual a 0 | |
| Imagen | Opcional; JPG, PNG o WebP, máximo 2 MB | |

### 3.4 Estados del pedido

| Estado | Significado | Transiciones permitidas | Quién |
|---|---|---|---|
| `PENDIENTE_PAGO` | Pedido creado, stock reservado, esperando pago | → `PAGADO`, `PAGO_RECHAZADO`, `EXPIRADO`, `CANCELADO` | Sistema (proveedor), cliente, Vendedor o Administrador (transferencia recibida) |
| `PAGO_RECHAZADO` | El proveedor rechazó el pago | → `PENDIENTE_PAGO` (reintento), `EXPIRADO`, `CANCELADO` | Cliente, sistema |
| `PAGADO` | Pago confirmado, stock descontado | → `EN_PREPARACION`, `CANCELADO` (con reembolso) | Sistema, Vendedor, Administrador |
| `EN_PREPARACION` | Se está preparando | → `ENVIADO` (despacho), `LISTO_PARA_RETIRO` (retiro), `CANCELADO` | Vendedor, Administrador |
| `ENVIADO` | En camino, con código de seguimiento | → `ENTREGADO` | Vendedor, Administrador |
| `LISTO_PARA_RETIRO` | Disponible en el punto de retiro | → `ENTREGADO` | Vendedor, Administrador |
| `ENTREGADO` | Recibido por el cliente | → `DEVUELTO` (retracto) | Vendedor, Administrador |
| `EXPIRADO` | No se pagó a tiempo; stock liberado | — (final) | Sistema |
| `CANCELADO` | Cancelado con motivo | — (final) | Cliente, Vendedor, Administrador |
| `DEVUELTO` | Devolución por retracto | — (final) | Vendedor, Administrador |

Las pantallas actuales del checkout se asocian así: *confirmación* = `PAGADO`; *pago fallido* = `PAGO_RECHAZADO`; hace falta una pantalla nueva para *pago pendiente* (transferencia).

### 3.5 Modelo de datos conceptual

| Entidad | Atributos clave | Notas |
|---|---|---|
| Producto | id, código (único), nombre, descripción, precio, precioAnterior, ofertaDesde, ofertaHasta, stock, stockCrítico, categoríaId, activo, creadoEn | `activo = false` es la baja lógica |
| ImagenProducto | id, productoId, url, orden | |
| Categoría | id, slug (único), nombre, descripción, orden | |
| ReservaStock | id, pedidoNúmero, productoId, cantidad, expiraEn, estado | Propiedad de `catalogo-service` |
| Usuario | id, run (opcional, único), nombres, apellidos, correo (único), passwordHash, teléfono, regiónCódigo, comunaCódigo, dirección, fechaNacimiento, rol, activo, creadoEn | `passwordHash` nunca sale del servicio |
| Región, Comuna | código, nombre; la comuna referencia su región | 16 y 346 registros, cargados por migración |
| Pedido | id, número (único), usuarioId (nulo si es invitado), contacto (nombre, correo, teléfono), tipoEntrega, dirección, comunaCódigo, métodoPago, estado, subtotal, ahorro, costoDespacho, total, creadoEn, expiraEn, seguimiento | Los montos los calcula el servidor |
| LíneaPedido | id, pedidoId, productoId, nombre, precioUnitario, precioAnterior, cantidad | *Snapshot* del producto |
| HistorialEstado | pedidoId, estadoAnterior, estadoNuevo, actor, fecha, motivo | Auditoría |
| TransacciónPago | id, pedidoNúmero, proveedor, referenciaExterna, monto, estado, códigoRespuesta, motivo, creadoEn | Propiedad de `pagos-service` |
| EntradaBlog | id, slug (único), título, resumen, imagenUrl, bloques (JSON), publicada, publicadaEn | |
| MensajeContacto | id, nombre, correo (opcional), comentario, estado, creadoEn | |

---

## 4. Requerimientos no funcionales

### 4.1 Seguridad

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-SEG-01 | Las contraseñas deberán almacenarse con un hash adaptativo (BCrypt o Argon2) y nunca registrarse en logs ni devolverse en respuestas. | M |
| RNF-SEG-02 | Todo el tráfico deberá viajar por HTTPS. | M |
| RNF-SEG-03 | La sesión deberá basarse en un token firmado de corta duración (por ejemplo, 15 minutos) y un token de renovación rotatorio. Se reemplaza el esquema actual de guardar solo el id del usuario en `kateye_sesion`, que permite suplantar cualquier cuenta editando `localStorage`. Dónde guardar el token se decide en DP-10. | M |
| RNF-SEG-04 | La autorización por rol deberá verificarse en el servidor en cada endpoint. | M |
| RNF-SEG-05 | El servidor deberá validar toda entrada (Bean Validation), usar consultas parametrizadas, escapar la salida y no aceptar HTML en el contenido de blogs. | M |
| RNF-SEG-06 | CORS deberá limitarse al origen del frontend. | M |
| RNF-SEG-07 | Login, registro, recuperación de contraseña y contacto deberán tener límite de intentos (por ejemplo, bloqueo temporal tras 5 fallos) y protección contra spam. | S |
| RNF-SEG-08 | Credenciales y claves deberán provenir de variables de entorno o un gestor de secretos, nunca del repositorio. | M |
| RNF-SEG-09 | El sistema no deberá recibir ni almacenar datos de tarjetas; esa responsabilidad es del proveedor de pagos. | M |
| RNF-SEG-10 | Ningún endpoint deberá aceptar parámetros del cliente que alteren el resultado de un pago (como la actual casilla "Simular pago rechazado"). El modo de pruebas se controla por configuración del servidor. | M |

### 4.2 Rendimiento

Metas iniciales, a validar con pruebas de carga (el volumen real del negocio no está informado):

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-REN-01 | Las consultas de catálogo deberán responder en menos de 500 ms (percentil 95) con 100 usuarios concurrentes. | S |
| RNF-REN-02 | La creación de un pedido, sin contar el tiempo del proveedor de pagos, deberá completarse en menos de 2 s. | S |
| RNF-REN-03 | Todo listado deberá estar paginado (20 por defecto, 100 como máximo). | M |

### 4.3 Confiabilidad y consistencia

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-CON-01 | Crear un pedido y reservar stock deberá ser consistente: si un paso falla, los anteriores se compensan (liberar reserva, cancelar pedido). No deberá existir sobreventa. | M |
| RNF-CON-02 | La creación de pedidos y la recepción de avisos del proveedor deberán ser idempotentes (clave de idempotencia). | M |
| RNF-CON-03 | Las reservas vencidas deberán liberarse automáticamente mediante una tarea programada. | M |
| RNF-DIS-01 | Disponibilidad objetivo de 99 % mensual. | C |
| RNF-DIS-02 | Respaldo diario de las bases de datos, con una pérdida máxima aceptable de 24 horas. | S |

### 4.4 Interfaz y compatibilidad con el frontend

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-INT-01 | La API deberá ser REST con JSON, versionada (`/api/v1`) y documentada con OpenAPI 3 antes de implementarse (contrato primero). | M |
| RNF-INT-02 | Los errores deberán tener un formato uniforme: código, mensaje general y, para validaciones, un mapa `campo → mensaje`, compatible con el objeto `errores` que ya usan `useFormulario` y `CampoFormulario`. | M |
| RNF-INT-03 | Los montos viajan como enteros CLP; las fechas, en ISO 8601 UTC. El frontend las muestra en hora de Chile. | M |
| RNF-INT-04 | Los cambios a la API deberán ser aditivos dentro de una versión (ver 2.4, regla 4). | S |
| RNF-INT-05 | El frontend deberá consumir la API a través de una capa de servicios (por ejemplo `src/api/`) que sustituya a la capa 1 actual, sin tocar los componentes. | M |
| RNF-INT-06 | Los mensajes de error visibles para el usuario deberán estar en español y ser comprensibles. | S |

### 4.5 Mantenibilidad y calidad

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-MAN-01 | Los servicios deberán desarrollarse con Java 17 o superior y Spring Boot 3.x. | M |
| RNF-MAN-02 | Cada servicio deberá incluir pruebas unitarias (JUnit 5 y Mockito) para sus reglas de negocio y pruebas de integración de repositorios y controladores. Meta inicial: 70 % de cobertura en la capa de servicio. | M |
| RNF-MAN-03 | El esquema de base de datos deberá versionarse con migraciones (Flyway o Liquibase). Datos semilla: 9 productos, 16 regiones, 346 comunas y un Administrador inicial. | M |
| RNF-MAN-04 | La configuración deberá separarse por perfiles (desarrollo, pruebas, producción); el proveedor de pagos usará su ambiente de pruebas fuera de producción. | M |
| RNF-MAN-05 | Todo el sistema deberá poder levantarse localmente con un solo comando (por ejemplo, Docker Compose) y cada servicio tendrá un README con sus instrucciones. | S |
| RNF-OBS-01 | Los servicios deberán generar logs estructurados con identificador de correlación entre servicios, exponer un endpoint de salud y no registrar datos personales ni contraseñas. | S |

### 4.6 Privacidad y cumplimiento normativo

> Estos puntos deben **validarse con asesoría legal**; el equipo no reemplaza a un abogado. Se incluyen porque un comercio electrónico en Chile los enfrenta de forma directa.

| ID | Requerimiento | Prio |
|---|---|---|
| RNF-LEG-01 | El sistema deberá recoger solo los datos personales necesarios, informar su finalidad en el registro y el checkout (política de privacidad) y permitir acceso, rectificación y eliminación de datos. La Ley 21.719 de protección de datos personales tiene entrada en vigencia fijada para el 1 de diciembre de 2026; existe un proyecto de ley que la posterga a 2027, aprobado por el Senado el 28 de octubre de 2026 y en trámite en la Cámara. **Verificar su estado antes de la entrega.** | M |
| RNF-LEG-02 | El sistema deberá informar el derecho de retracto en compras a distancia, que la Ley 19.496 fija en 10 días desde la recepción del producto, y enviar la confirmación escrita del contrato (si no se envía, el plazo se extiende a 90 días). El reglamento de 2024 establece exclusiones; **validar cuáles aplican a cosméticos**. | M |
| RNF-LEG-03 | Los plazos de conservación de pedidos y datos tributarios deberán definirse con asesoría contable (DP-13). | S |

---

## 5. Trazabilidad con el frontend y cambios que implica

### 5.1 De cada módulo actual a su servicio

| Frontend actual | Qué ocurre | Endpoints propuestos | Servicio |
|---|---|---|---|
| `productos.js` | Se reemplaza por cliente HTTP | `GET /productos`, `GET /productos/{id}`, `POST/PUT/DELETE /productos` (Admin, Vendedor) | catalogo |
| `obtenerCategorias` y lista fija del `Header` | Se reemplaza | `GET /categorias` | catalogo |
| `carrito.js` | Se conservan solo los cálculos de visualización; las reglas autoritativas pasan al servidor | `POST /carrito/validar` | pedidos |
| `validaciones.js` | Se mantiene como ayuda de UX; la fuente de verdad es la sección 3.3 | — | — |
| `usuarios.js`, `SesionProvider` | Se reemplazan; la sesión guarda un token y consulta `/usuarios/me` al arrancar | `POST /auth/registro`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `GET /usuarios/me` | usuarios |
| `pedidos.js` | Se reemplaza; el pedido se crea **antes** del pago | `POST /pedidos`, `GET /pedidos/mios`, `GET /pedidos/{numero}` | pedidos |
| `pagos.js` | Se reemplaza; se elimina `forzarFallo` | `POST /pedidos/{numero}/pago`, `POST /pagos/webhook` (proveedor) | pagos |
| `regiones.js` | Se reemplaza | `GET /regiones`, `GET /regiones/{codigo}/comunas` | catalogo |
| `blogs.js` | Se reemplaza | `GET /blogs`, `GET /blogs/{slug}` | catalogo |
| Vista `Contacto` | Envía al servidor | `POST /contacto` | notificaciones |
| `utils/imagenes.js` | Resuelve URLs del servicio de imágenes; conserva el recuadro "Sin imagen" | — | catalogo |
| `CarritoProvider` | Sigue en `localStorage`; valida contra el servidor al abrir el checkout | — | — |

### 5.2 Cambios transversales en el frontend

1. **Todo se vuelve asíncrono.** Hoy `obtenerProductos()` o `autenticar()` responden al instante; con HTTP las vistas necesitan estados de *cargando* y *error*, y las pruebas existentes deberán simular la API.
2. **El flujo del checkout cambia de orden:** crear pedido → iniciar pago → volver desde el proveedor → consultar el estado. Hoy se crea el pedido solo si el pago "aprueba".
3. **Vistas nuevas necesarias:** resultado del pago y seguimiento del pedido, mis pedidos, perfil, recuperar contraseña, ofertas, categorías (las pide el enunciado de la evaluación), página 404 propia y el panel de administración.
4. **Se retira** la casilla "Simular pago rechazado" y las claves `kateye_usuarios` y `kateye_ultimo_pedido` de `localStorage`.
5. **El checkout** debe ofrecer elegir despacho o retiro y mostrar el costo de despacho.

---

## Anexo A. Decisiones pendientes

| ID | Decisión | Opciones | Recomendación |
|---|---|---|---|
| DP-01 | Dominios de correo permitidos | Mantener los 3 dominios (ejercicio académico) o aceptar cualquier correo válido | Mantenerlos mientras sea requisito del curso, aislados en una sola configuración para poder retirarlos |
| DP-02 | Política de contraseña | 4–10 caracteres (curso) o 8–64 | 8–64 si el curso lo permite: un máximo de 10 reduce la seguridad sin beneficio |
| DP-03 | Proveedor de pagos | Webpay Plus, Flow, Mercado Pago o simulador propio detrás de una interfaz | Definir una interfaz `ProveedorPago` y empezar con un simulador; cambiar el proveedor no debe tocar el resto |
| DP-04 | Dónde vive el carrito | Navegador (con validación) o servidor | Navegador en v1; servidor como mejora (RF-CAR-04) |
| DP-05 | Datos de usuario | Nombre único o nombres + apellidos; RUN obligatorio, opcional o solo para personal | Nombres y apellidos separados; RUN opcional para clientes hasta decidir la boleta (DP-13) |
| DP-06 | Despacho | Cobertura (todo Chile o algunas regiones); costo fijo, por zona o gratis sobre un monto; ubicación del punto de retiro | Cobertura y tarifa simples por región |
| DP-07 | Transferencia bancaria | Confirmación manual o automática; plazo para pagar | Confirmación manual en v1, con plazo de 48 horas |
| DP-08 | Duración de la reserva de stock | 15 a 60 minutos (tarjeta); más para transferencia | 15 minutos para tarjeta |
| DP-09 | Almacenamiento de imágenes | Carpeta servida por el servicio o almacenamiento de objetos | Carpeta en v1, detrás de una interfaz |
| DP-10 | Dónde guardar el token en el navegador | `localStorage` o cookie `httpOnly` | Cookie `httpOnly` para el token de renovación; implica CSRF y ajustes de CORS |
| DP-11 | Nombre oficial | KatEye, KatEyes o Kateyes | **KatEye** (usado en este documento); renombrar el frontend en un solo cambio |
| DP-12 | Correo en el formulario de contacto | Opcional (hoy) u obligatorio | Obligatorio: sin correo no hay forma de responder |
| DP-13 | Boleta o factura, IVA y conservación de datos | Dentro o fuera de v1 | Fuera de v1, pero guardar monto neto e IVA en el pedido; consultar a un contador |
| DP-14 | Cantidad de microservicios | 3 (catálogo, usuarios, pedidos con pagos) o los 5 de la sección 2.3 | Empezar con 3 si el curso lo permite y separar después; cada servicio extra suma despliegue, pruebas y comunicación |
| DP-15 | Contenido de los blogs | Actualizar o retirar entradas desfasadas | Ver B-18 |

## Anexo B. Inconsistencias detectadas y cómo las resuelve esta versión

| # | Inconsistencia | Dónde | Resolución |
|---|---|---|---|
| B-01 | El nombre del negocio aparece como "Kateye", "KatEyes" y "Kateyes"; las claves de `localStorage` usan `kateye_` | v1, HTML, documento de arquitectura | DP-11 |
| B-02 | Se describe una "pasarela de pago" como algo que la aplicación construye | v1 | Es un servicio externo; la aplicación gestiona pedidos y pagos (2.1) |
| B-03 | La v1 pide "gestionar los estados de la compra", pero el frontend solo conoce aprobado o rechazado y **no guarda nada si el pago falla** | v1 vs. `Checkout` | Pedido creado antes del pago y modelo de estados (3.4) |
| B-04 | La v1 menciona "despacho o retiro", pero el checkout no permite elegir ni muestra costo de envío | v1 vs. `Checkout` | RF-PED-04, RF-ENV-03, RF-ENV-04 |
| B-05 | La v1 habla de clientes y administradores; el panel admin define tres roles; `usuarios.js` no tiene roles | v1, panel admin, `usuarios.js` | Tres roles (1.5) |
| B-06 | Registro con un solo "nombre completo"; el panel admin pide nombre, apellidos, RUN y dirección | `Registro` vs. panel admin | DP-05 |
| B-07 | El panel admin acepta precios con decimales; el CLP no tiene decimales y `formatearPrecio` los omite | Panel admin | RN-01, 3.3 |
| B-08 | Contraseñas de 4 a 10 caracteres: el máximo de 10 limita la seguridad | `validarPassword` | DP-02 |
| B-09 | Solo se aceptan correos `duoc.cl`, `profesor.duoc.cl` y `gmail.com`, lo que excluiría a clientes reales | `validarCorreo` | DP-01 |
| B-10 | La lista de categorías del `Header` (con sus subtítulos) está escrita a mano, aparte del catálogo | `Header` | RF-CAT-04 |
| B-11 | Las imágenes se resuelven desde `src/assets` por nombre de archivo; un administrador no puede subir imágenes nuevas | `utils/imagenes.js` | RF-CAT-08 |
| B-12 | No existe stock ni código de producto; el máximo de 10 unidades es fijo. El panel admin los exige | `productos.js` vs. panel admin | RF-CAT-07, RF-CAT-10, RN-02 |
| B-13 | La sesión guarda solo el id del usuario; editar `localStorage` permite hacerse pasar por otro | `kateye_sesion` | RNF-SEG-03 |
| B-14 | La confirmación dice "Enviaremos el detalle a…", pero no existe envío de correos | `ConfirmacionPedido` | RF-PED-09 |
| B-15 | Existe una casilla para forzar el rechazo del pago, y tarjeta y transferencia se tratan igual | `Checkout`, `pagos.js` | RF-PAG-05, RNF-SEG-10 |
| B-16 | Solo se guarda el último pedido; no hay historial ni distinción entre invitado y cliente | `kateye_ultimo_pedido` | RF-PED-03, RF-PED-07 |
| B-17 | El enunciado de la evaluación pide las vistas Categorías y Ofertas; el frontend no tiene rutas para ellas | Mapa de rutas | RF-CAT-04, RF-CAT-05 |
| B-18 | Una entrada del blog anuncia descuentos "durante todo octubre" mientras los productos tienen precio anterior sin vigencia; otra reseña una marca que no está en el catálogo (versión HTML; verificar en `blogs.js`) | Blogs | RF-CAT-05, DP-15 |
| B-19 | El correo del formulario de contacto es opcional, así que algunos mensajes no se pueden responder | `Contacto` | DP-12 |
| B-20 | El teléfono del footer (`+1 234 567 890`) es de ejemplo y no es chileno | `Footer` | Datos de contacto configurables, no escritos en el código |
| B-21 | La microarquitectura aparece dentro de la "solución" como si fuera un requerimiento | v1 | Se mueve a 2.3 como restricción de diseño |
| B-22 | La expresión de teléfono del checkout es laxa | `validarCampoCheckout` | Normalización en 3.3 |
| B-23 | La v1 usa "debería", deja vacías las secciones de requerimientos y tiene errores de redacción | v1 | Lenguaje normativo, prioridades y redacción corregida |

## Anexo C. Supuestos

- Es un **caso hipotético**: no hay cliente real que valide estos requerimientos. La "conformidad de los administradores" con el frontend tentativo se toma como supuesto del caso, no como aprobación de esta especificación.
- No se conoce el volumen de ventas; las metas de rendimiento (4.2) son iniciales.
- El proveedor de pagos se usa en ambiente de pruebas; no se realizan cobros reales.
- Las referencias normativas (4.6) son orientativas y deben ser revisadas por un especialista.

## Anexo D. Glosario

| Término | Significado |
|---|---|
| **ERS** | Especificación de Requerimientos de Software. |
| **Fuente de verdad** | El sistema cuya versión de un dato prevalece sobre las demás (aquí, el backend). |
| **Snapshot** | Copia de los datos de un producto en el momento de la compra, para que cambios posteriores no alteren pedidos pasados. |
| **Baja lógica** | Marcar un registro como inactivo en lugar de borrarlo. |
| **Webhook** | Aviso que el proveedor de pagos envía al servidor cuando cambia el estado de un pago. |
| **Idempotencia** | Propiedad por la que repetir una misma operación produce el mismo efecto que hacerla una vez. |
| **Compensación** | Acción que deshace un paso previo cuando falla uno posterior en un proceso de varios servicios. |
| **Token (JWT)** | Credencial firmada que prueba quién es el usuario sin consultar la base de datos en cada petición. |
| **Contrato primero** | Escribir la especificación de la API (OpenAPI) antes de programar cliente o servidor. |
