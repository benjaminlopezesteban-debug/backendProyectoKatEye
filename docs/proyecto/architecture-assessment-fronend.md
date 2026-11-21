# Kateyes — Arquitectura técnica del frontend React

*Relaciones entre archivos y componentes, función de cada pieza y flujos de funcionamiento. Las dependencias se extrajeron leyendo los `import` reales del código (octubre de 2026).*

## 1. Resumen

Tienda de cosméticos migrada de HTML/CSS/JS a una aplicación de una sola página (SPA) con React. No hay backend: los datos salen de archivos JavaScript y el estado del usuario se guarda en `localStorage`.

| Tema | Decisión |
| --- | --- |
| Base | React + Vite |
| Navegación | React Router (`BrowserRouter`, `Routes`, `Link`, `NavLink`, `useParams`, `useSearchParams`) |
| Estilos | Bootstrap 5.3 y Bootstrap Icons por npm, más `src/index.css` (tokens de diseño, navbar, blogs, footer) |
| Datos | Archivos JS simulando una base de datos (`productos.js`, `blogs.js`, `regiones.js`, `usuarios.js`) |
| Estado compartido | React Context: carrito y sesión |
| Imágenes | En `src/assets/`, resueltas por nombre con `import.meta.glob` |
| Pruebas | Vitest + Testing Library + user-event + jest-dom (jsdom), 165 pruebas en 13 archivos |

## 2. Estructura de carpetas

```
src/
├── main.jsx                  punto de entrada: estilos, Router y Providers
├── App.jsx                   layout (Header, Footer, panel del carrito) y mapa de rutas
├── index.css                 estilos globales
│
├── productos.js              catálogo + CRUD en memoria
├── blogs.js                  entradas del blog
├── regiones.js               16 regiones y 346 comunas
├── carrito.js                reglas del carrito (funciones puras)
├── validaciones.js           reglas de validación de formularios
├── usuarios.js               usuarios simulados (registro y autenticación)
├── pedidos.js                creación del pedido confirmado
├── pagos.js                  simulación de la pasarela de pago
│
├── utils/
│   ├── formatearPrecio.js    número → '$12.990'
│   └── imagenes.js           nombre de archivo → URL de src/assets
├── context/
│   ├── CarritoContext.js     contexto + hook useCarrito()
│   ├── CarritoProvider.jsx   estado del carrito
│   ├── SesionContext.js      contexto + hook useSesion()
│   └── SesionProvider.jsx    estado de la sesión
├── hooks/
│   └── useFormulario.js      lógica reutilizable de formularios controlados
├── components/               piezas reutilizables (14 archivos, ver sección 6.5)
├── views/                    una vista por página (10 archivos, ver sección 6.6)
├── assets/                   imágenes de productos, blogs y logo
└── test/                     pruebas unitarias + setup.js
```

## 3. Capas y regla de dependencias

El proyecto se organiza en capas. **Las dependencias solo apuntan hacia abajo**: ninguna capa importa de una capa superior (verificado en el grafo de `import`).

| Capa | Archivos | ¿Usa React? | Puede importar de |
| --- | --- | --- | --- |
| 1. Datos y reglas | `productos.js`, `blogs.js`, `regiones.js`, `carrito.js`, `validaciones.js`, `usuarios.js`, `pedidos.js`, `pagos.js` | No (JavaScript puro) | Solo de su misma capa (`carrito.js` importa `productos.js`) |
| 2. Utilidades | `utils/formatearPrecio.js`, `utils/imagenes.js` | No | Nada |
| 3. Estado compartido | `context/*`, `hooks/useFormulario.js` | Sí (Context y hooks) | Capa 1 |
| 4. Componentes | `components/*.jsx` | Sí | Capas 1, 2, 3 y otros componentes |
| 5. Vistas | `views/*.jsx` | Sí | Capas 1 a 4 |
| 6. Raíz | `main.jsx`, `App.jsx` | Sí | Todo lo anterior |

**Por qué importa:** las capas 1 y 2 no dependen de React ni del navegador (salvo `localStorage`), por eso se pueden probar directamente con funciones simples y reutilizar en un futuro backend o en otra interfaz.

## 4. Arranque de la aplicación

```
index.html  (contiene <div id='root'>)
  └─ main.jsx
       ├─ importa estilos: Bootstrap CSS, Bootstrap Icons, index.css
       ├─ importa Bootstrap JS (activa los atributos data-bs-* del dropdown, offcanvas, modal y colapso)
       └─ <BrowserRouter>                     habilita Link, NavLink, Routes
            └─ <SesionProvider>              quién tiene la sesión iniciada
                 └─ <CarritoProvider>        qué hay en el carrito
                      └─ <App />
                           ├─ <Header />            en todas las páginas
                           ├─ <main><Routes>…</Routes></main>   la vista según la URL
                           ├─ <Footer />            en todas las páginas
                           ├─ <PanelCarrito />      panel lateral, montado una sola vez
                           └─ <CarritoFlotante />   botón flotante con contador
```

`App` lee ambos contextos: pasa al `Header` la cantidad del carrito (`useCarrito`) y el usuario con su función de cerrar sesión (`useSesion`). Así `Header` es un componente que solo recibe props.

## 5. Mapa de rutas

| Ruta | Vista | Componentes que dibuja | Datos y estado que usa |
| --- | --- | --- | --- |
| `/` | `Inicio` | `InfoBox`, `FilaProducto` (→ `BotonAgregar`, `Precio`) | `productos.js`, `useCarrito` |
| `/productos` | `Productos` | `TarjetaProducto` (→ `BotonAgregar`, `Precio`) | `productos.js`, `useCarrito`, parámetro `?categoria=` |
| `/productos/:id` | `DetalleProducto` | `Precio`, `TarjetaRelacionada` | `productos.js`, `carrito.js` (máximo), `useCarrito` |
| `/checkout` | `Checkout` | `CampoFormulario`, `ResumenPedido`, `ConfirmacionPedido`, `PagoFallido` | `validaciones.js`, `pagos.js`, `pedidos.js`, `useCarrito`, `useSesion` |
| `/blogs` | `Blogs` | — | `blogs.js` |
| `/blogs/:slug` | `Blog` | — | `blogs.js` |
| `/nosotros` | `Nosotros` | — | Sin dependencias |
| `/contacto` | `Contacto` | `CampoFormulario` | `validaciones.js` |
| `/iniciar-sesion` | `IniciarSesion` | `CampoFormulario` | `useFormulario`, `validaciones.js`, `usuarios.js`, `useSesion` |
| `/registro` | `Registro` | `CampoFormulario` | Lo anterior más `regiones.js` |
| cualquier otra | `EnConstruccion` (definida dentro de `App.jsx`) | — | — |

En **todas** las rutas se ven además `Header`, `Footer`, `PanelCarrito` y `CarritoFlotante`, porque están en `App` fuera de `<Routes>`.

## 6. Catálogo de archivos

### 6.1 Raíz

| Archivo | Función |
| --- | --- |
| `main.jsx` | Monta React en `#root`, importa los estilos y envuelve la app con `BrowserRouter`, `SesionProvider` y `CarritoProvider`. |
| `App.jsx` | Define el layout común y el mapa de rutas (sección 5). Lee carrito y sesión para alimentar al `Header`. |
| `index.css` | Estilos globales: tokens de diseño (paleta, tipografías, anulaciones de variables de Bootstrap), navbar, hero, filtros, login, modal, blogs, nosotros, footer y la apertura por cursor del menú Categorías. |

### 6.2 Datos y reglas (capa 1)

| Archivo | Qué hace | Relaciones |
| --- | --- | --- |
| `productos.js` | Catálogo de 9 productos y CRUD en memoria: `obtenerProductos`, `obtenerProductoPorId`, `obtenerCategorias`, `crearProducto`, `actualizarProducto`, `eliminarProducto`. Las imágenes se guardan solo como nombre de archivo. Los cambios se pierden al recargar. | Lo usan `carrito.js`, `Inicio`, `Productos`, `DetalleProducto`. |
| `carrito.js` | Las reglas del carrito como funciones puras sobre un arreglo `[{ id, cantidad }]`: `agregarItem`, `cambiarCantidadItem`, `eliminarItem`, `sanearCarrito`, `detallarCarrito` (une cada línea con el catálogo), `totalCarrito`, `ahorroCarrito`, `contarUnidades`, `leerCarritoGuardado`, `guardarCarritoEnStorage`. Constantes `MAX_UNIDADES_POR_PRODUCTO` (10) y `CLAVE_CARRITO`. | Importa `productos.js`. Lo usan `CarritoProvider`, `PanelCarrito`, `BotonAgregar`, `DetalleProducto`. |
| `validaciones.js` | Cada regla devuelve `''` si el valor es válido o el mensaje de error. Por formulario: `validarCheckout`, `validarContacto`, `validarLogin`, `validarRegistro` (y sus versiones `validarCampo…` para un solo campo). Reutilizables: `validarCorreo` (solo @duoc.cl, @profesor.duoc.cl y @gmail.com), `validarPassword` (4 a 10 caracteres), `validarTextoRequerido`. Las reglas reciben también los datos completos para comparar campos (confirmar contraseña). | Lo usan `Checkout`, `Contacto`, `IniciarSesion`, `Registro`. |
| `usuarios.js` | Usuarios simulados en `localStorage`: `registrarUsuario` (rechaza correos repetidos sin distinguir mayúsculas), `autenticar`, `obtenerUsuarioPorId`. Nunca devuelve la contraseña. | Lo usan `SesionProvider`, `IniciarSesion`, `Registro`. |
| `pedidos.js` | `crearPedido`: arma el pedido (número `KE-######`, fecha, cliente, ítems, total), lo guarda como último pedido y lo devuelve. | Lo usa `Checkout`. |
| `pagos.js` | `procesarPago`: simula al banco. Devuelve `{ ok: true }` o `{ ok: false, codigo, motivo }`. Con `forzarFallo` rechaza el pago, para demostrar la pantalla de pago fallido. | Lo usa `Checkout`. |
| `regiones.js` | 16 regiones y 346 comunas de Chile. `REGIONES`, `obtenerRegion`, `obtenerComunas`. | Lo usa `Registro`. |
| `blogs.js` | Entradas del blog como datos; el contenido es un arreglo de bloques (título, párrafo, lista). `obtenerBlogs`, `obtenerBlogPorSlug`. | Lo usan `Blogs` y `Blog`. |

### 6.3 Utilidades (capa 2)

| Archivo | Qué hace | Lo usan |
| --- | --- | --- |
| `utils/formatearPrecio.js` | `formatearPrecio(12990)` devuelve `'$12.990'` (formato es-CL). | `Precio`, `PanelCarrito`, `ResumenPedido`, `ConfirmacionPedido`, `DetalleProducto` |
| `utils/imagenes.js` | `resolverImagen(nombre)` convierte un nombre de archivo en la URL final de `src/assets`. Usa `import.meta.glob` porque Vite renombra los archivos al compilar. Si la imagen no existe devuelve `IMG_PLACEHOLDER` (un recuadro gris 'Sin imagen'). | `Header`, `TarjetaProducto`, `FilaProducto`, `TarjetaRelacionada`, `PanelCarrito`, `ResumenPedido`, `Blogs`, `Blog`, `DetalleProducto` |

### 6.4 Estado compartido (capa 3)

| Archivo | Qué hace |
| --- | --- |
| `context/CarritoContext.js` | Crea el contexto del carrito y el hook `useCarrito()`, que lanza un error si se usa fuera del provider. Va separado del provider para no mezclar componentes y hooks en un mismo archivo. |
| `context/CarritoProvider.jsx` | Dueño del estado `[{ id, cantidad }]`. Lo inicializa leyendo `localStorage` ya saneado, lo respalda con `useEffect` y expone `lineas`, `cantidadTotal`, `total`, `ahorro`, `agregarAlCarrito`, `cambiarCantidad`, `eliminarDelCarrito`, `vaciarCarrito`. Importa `carrito.js`. |
| `context/SesionContext.js` | Contexto de la sesión y hook `useSesion()`. |
| `context/SesionProvider.jsx` | Estado `usuario` (o `null`). Guarda solo el id en `localStorage`; al abrir la app lo busca en `usuarios.js`, de modo que una sesión de un usuario inexistente se descarta. Expone `usuario`, `iniciarSesion`, `cerrarSesion`. |
| `hooks/useFormulario.js` | Hook para formularios controlados. Recibe `datosIniciales`, `validarCampo` y `validarTodo`; guarda `datos` y `errores` y entrega `manejarCambio`, `manejarBlur`, `manejarEnvio(alExito)`, `reiniciar`, `setDatos`, `setErrores`. Lo usan `IniciarSesion` y `Registro`. |

### 6.5 Componentes (capa 4)

| Componente | Props | Qué hace | Usa |
| --- | --- | --- | --- |
| `Header` | `cantidadCarrito`, `usuario`, `onCerrarSesion` | Barra de navegación. Lista de categorías desde un arreglo; `NavLink` marca la página activa; con sesión muestra saludo y `Salir`, sin sesión `Ingresar` y `Registrarse`. El menú Categorías se abre con el cursor en escritorio (CSS). | `utils/imagenes` |
| `Footer` | — | Pie de página estático con el año calculado. | — |
| `InfoBox` | `retraso` | Modal de novedades de Bootstrap que aparece tras 1,5 s. Usa `useRef` y `useEffect` con limpieza. Solo se muestra en `Inicio`. | `bootstrap` |
| `PanelCarrito` | — | Panel lateral (offcanvas): líneas con +, − y eliminar, ahorro, total y botón `Ir a pagar` (deshabilitado con el carrito vacío; lleva a `/checkout`). | `useCarrito`, `carrito.js`, `Precio` |
| `CarritoFlotante` | — | Botón flotante que abre el panel y muestra la cantidad de unidades. | `useCarrito` |
| `Precio` | `producto`, `tamano` | Precio normal o con oferta (anterior tachado, actual y porcentaje de descuento). | `formatearPrecio` |
| `BotonAgregar` | `producto`, `onAgregar`, `texto`, `className` | Botón de compra con mensaje temporal de 1,2 s: `✓ Añadido` o `Máx. 10 unidades`. | `carrito.js` |
| `TarjetaProducto` | `producto`, `onAgregar` | Tarjeta del catálogo. | `Precio`, `BotonAgregar` |
| `FilaProducto` | `producto`, `onAgregar` | Producto en formato de fila, para el inicio. | `Precio`, `BotonAgregar` |
| `TarjetaRelacionada` | `producto` | Tarjeta compacta, sin botón de compra. | `Precio` |
| `CampoFormulario` | `id`, `nombre`, `etiqueta`, `valor`, `error`, `onChange`, `onBlur`, `tipo`, `clase`, `ayuda`, `children` | Campo con etiqueta, control y error. `tipo` puede ser `text`, `email`, `password`, `tel`, `textarea` o `select`. Es controlado: el valor lo guarda quien lo usa. | — |
| `ResumenPedido` | `lineas`, `total`, `ahorro` | Tarjeta de resumen del checkout. | `formatearPrecio`, `imagenes` |
| `ConfirmacionPedido` | `pedido` | Pantalla de compra exitosa. | `formatearPrecio` |
| `PagoFallido` | `resultado`, `onReintentar` | Pantalla de pago rechazado, con motivo, código y botón para reintentar. | — |

### 6.6 Vistas (capa 5)

| Vista | Qué hace | Detalle de funcionamiento |
| --- | --- | --- |
| `Inicio` | Hero de bienvenida, lista de todos los productos y el modal de novedades. | Pasa `agregarAlCarrito` a cada `FilaProducto`. |
| `Productos` | Catálogo con filtros por categoría y rango de precio. | Dos componentes en un archivo: `Productos` (lee `?categoria=` de la URL) y `Catalogo` (guarda el estado de los filtros). La lista visible se calcula en cada render y no se guarda como estado. |
| `DetalleProducto` | Galería, selector de cantidad, subtotal y productos relacionados de la misma categoría. | Envoltorio (lee `:id`, decide si existe) y `Detalle` (estado de imagen, cantidad y mensaje). Muestra un aviso si el producto no existe. |
| `Checkout` | Resumen del pedido, formulario de datos y pago. | Una de cuatro pantallas según el estado: confirmación, pago fallido, carrito vacío o formulario. |
| `Blogs` | Listado de entradas. | Un recuadro repetido con `.map()` sobre `blogs.js`. |
| `Blog` | Una entrada. | Una sola vista para todas las entradas; dibuja cada bloque según su tipo. |
| `Nosotros` | Descripción de la empresa y desarrolladores. | Vista estática. |
| `Contacto` | Formulario con contador de 500 caracteres. | Formulario controlado propio (marcado para migrar a `useFormulario`). |
| `IniciarSesion` | Login contra los usuarios registrados. | Usa `useFormulario`; muestra un aviso si las credenciales no coinciden. |
| `Registro` | Alta de usuario con región y comuna dependientes. | Usa `useFormulario`; al cambiar la región borra la comuna; si el correo existe marca el error; si no, inicia sesión y va al inicio. |

## 7. Estado global

### Carrito

- **Estado:** arreglo `[{ id, cantidad }]`. No se guardan nombre ni precio: siempre se leen del catálogo con `detallarCarrito`, por lo que alterar `localStorage` a mano no cambia precios.
- **Reglas** (en `carrito.js`): cantidad entera entre 1 y 10; al superar el máximo se limita; una línea que llega a 0 se elimina; un producto no se duplica; los ítems corruptos o de productos inexistentes se descartan; no se puede ir a pagar con el carrito vacío.
- **Quién lo lee:** `App` (contador del Header), `CarritoFlotante`, `PanelCarrito`, `Inicio`, `Productos`, `DetalleProducto`, `Checkout`.

### Sesión

- **Estado:** `usuario` o `null`. Se guarda solo el id, y los datos se leen de `usuarios.js`.
- **Quién lo lee:** `App` (saludo del Header), `Checkout` (precarga nombre, correo, teléfono y comuna), `IniciarSesion` y `Registro` (inician la sesión).

## 8. Flujos principales

### 8.1 Añadir un producto al carrito

1. El usuario pulsa `BotonAgregar` (en una tarjeta, una fila o el detalle).
2. Se llama a `agregarAlCarrito(producto, cantidad)` del contexto.
3. `CarritoProvider` ejecuta `agregarItem` (de `carrito.js`), que aplica las reglas y devuelve el carrito nuevo y cuántas unidades se agregaron.
4. Si se agregó algo, se actualiza el estado. React vuelve a dibujar el contador del Header, `CarritoFlotante` y `PanelCarrito`, y un `useEffect` respalda el carrito en `localStorage`.
5. La función devuelve las unidades agregadas y `BotonAgregar` muestra `✓ Añadido` (o `Máx. 10 unidades` si fueron 0) durante 1,2 s.

### 8.2 Checkout y pago

1. En `PanelCarrito`, `Ir a pagar` lleva a `/checkout`.
2. `Checkout` lee líneas, total y ahorro de `useCarrito`. Si hay sesión, el formulario parte con los datos del usuario.
3. Cada campo se valida al salir de él (`validarCampoCheckout`) y todo el formulario al enviar (`validarCheckout`). El foco va al primer error.
4. Con datos válidos se llama a `procesarPago`:
   - **Rechazado:** se muestra `PagoFallido`. No se crea pedido y el carrito no se toca. `Reintentar pago` vuelve al formulario con los datos intactos.
   - **Aprobado:** `crearPedido` guarda el pedido, `vaciarCarrito()` deja el carrito en 0 y se muestra `ConfirmacionPedido`.
5. Orden de pantallas en `Checkout`: pedido creado, pago fallido, carrito vacío, formulario. La confirmación va primero porque al vaciar el carrito `lineas` queda vacío.

### 8.3 Registro e inicio de sesión

- **Registro:** `useFormulario` valida con `validarRegistro`. `registrarUsuario` guarda al usuario; si el correo ya existe, se marca el error en ese campo. Si es nuevo, `iniciarSesion` guarda la sesión y se navega a `/`.
- **Login:** `autenticar(correo, password)` busca coincidencia. Si no hay, aparece 'Correo o contraseña incorrectos'; si hay, `iniciarSesion` y navegación a `/`.
- **Salir:** `cerrarSesion` limpia el estado y borra la clave de sesión. El Header vuelve a mostrar `Ingresar` y `Registrarse`.

### 8.4 Filtro de categoría desde el menú

El Header enlaza a `/productos?categoria=Labios`. `Productos` lee el parámetro con `useSearchParams` y lo entrega a `Catalogo` como `categoriaInicial`. `Catalogo` recibe una prop `key` que depende de la categoría: al cambiar, React desmonta y vuelve a crear el componente con el estado de filtros reiniciado. Sin esa `key`, elegir otra categoría estando ya en `/productos` no actualizaría las casillas.

### 8.5 Imágenes

`productos.js` guarda `'Base líquida mate.jpg'`. `resolverImagen` busca ese nombre entre los archivos de `src/assets` (ya procesados por Vite) y devuelve la URL final; si no la encuentra, devuelve el recuadro gris.

## 9. Persistencia

| Clave en `localStorage` | Contenido | Quién la escribe |
| --- | --- | --- |
| `kateye_carrito` | `[{ id, cantidad }]` | `CarritoProvider` mediante `carrito.js` |
| `kateye_sesion` | id numérico del usuario | `SesionProvider` |
| `kateye_usuarios` | usuarios registrados (incluye la contraseña, solo prototipo) | `usuarios.js` |
| `kateye_ultimo_pedido` | último pedido confirmado | `pedidos.js` |

Productos y blogs viven en el código. Los cambios que haga el CRUD de `productos.js` solo duran hasta recargar la página.

## 10. Pruebas

Comando: `npx vitest run` (o `npm test`). `src/test/setup.js` carga `jest-dom` y limpia el DOM después de cada prueba.

| Archivo de prueba | Prueba | Pruebas |
| --- | --- | --- |
| `carrito.test.js` | `carrito.js`: reglas, saneamiento, totales y `localStorage` | 32 |
| `validaciones.test.js` | `validaciones.js`: checkout, contacto, login y registro | 29 |
| `productos.test.js` | CRUD de `productos.js` y datos de `regiones.js` | 20 |
| `usuarios.test.js` | `usuarios.js` | 16 |
| `pedidos.test.js` | `pedidos.js` y `pagos.js` | 11 |
| `Productos.test.jsx` | Vista: lista, filtros, aviso condicional, integración con el carrito | 13 |
| `Header.test.jsx` | Sesión condicional, categorías, enlace activo, evento Salir | 9 |
| `CampoFormulario.test.jsx` | Estado al escribir, error condicional, `textarea`, `select` | 9 |
| `BotonAgregar.test.jsx` | Props, clic, mensajes y temporizador | 7 |
| `Precio.test.jsx` | Oferta y sin oferta, tamaño | 5 |
| `TarjetaProducto.test.jsx` | Props, enlace al detalle, compra | 5 |
| `PagoFallido.test.jsx` | Alerta, motivo, reintento | 5 |
| `ConfirmacionPedido.test.jsx` | Datos del pedido | 4 |
| **Total** |  | **165** |

Los componentes con `Link` se prueban dentro de `MemoryRouter`; `Productos` también dentro de `CarritoProvider`.

**Sin pruebas todavía:** `InfoBox`, `PanelCarrito`, `CarritoFlotante`, `Footer`, `FilaProducto`, `TarjetaRelacionada`, `ResumenPedido`, `useFormulario`, `SesionProvider` y las vistas `Inicio`, `DetalleProducto`, `Checkout`, `Blogs`, `Blog`, `Nosotros`, `Contacto`, `IniciarSesion` y `Registro`. El hover del menú es CSS y no se prueba con jsdom.

## 11. Patrones aplicados

- **Lógica pura separada de React** (`carrito.js`, `validaciones.js`, `usuarios.js`…): se prueba sin renderizar y no cambia si cambia la interfaz.
- **Estado derivado:** la lista de productos visibles, el subtotal, el contador de caracteres y las comunas no son estado; se calculan a partir de otros datos en cada render.
- **Formulario controlado:** el valor vive en el estado de React y `CampoFormulario` solo lo muestra y avisa los cambios. `useFormulario` empaqueta los manejadores comunes.
- **Context** para lo que usan muchos componentes lejanos entre sí (carrito y sesión), en lugar de pasar props por cada nivel.
- **Envoltorio con `key`** en `Productos` y `DetalleProducto` para reiniciar el estado cuando cambia el parámetro de la URL sin cambiar de ruta.
- **Separar contexto y provider** en dos archivos, para que el hook y el componente no convivan en uno solo.

## 12. Limitaciones conocidas

- **Panel de administración no migrado.** El CRUD de `productos.js` existe pero no tiene interfaz ni persistencia.
- **Contraseñas en texto plano** en `localStorage`. Es válido solo para este prototipo; en un sistema real las gestiona el servidor con un hash.
- **El pago es simulado.** La casilla 'Simular pago rechazado' del checkout existe para poder demostrar el flujo de fallo y debe retirarse cuando haya un backend de pagos.
- **`Checkout` y `Contacto` aún tienen sus propios manejadores** en lugar de `useFormulario`; ambos están marcados en el código con la migración pendiente.
- **Menú móvil:** no se cierra solo al navegar a otra página.
- **Teléfono del footer** (`+1 234 567 890`) es un dato de ejemplo.
- **No existe una página 404 propia:** las rutas desconocidas muestran el aviso 'aún no se ha migrado'.
- **Herramienta de pruebas:** el enunciado de la evaluación menciona Jasmine y Karma, pero el proyecto sigue el patrón entregado por el docente (Vitest y Testing Library).
