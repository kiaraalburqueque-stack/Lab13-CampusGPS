# CampusGPS

**Laboratorio 13 – Localización Geográfica · Programación en Móviles (TECSUP)**
Autora: Kiara Alburqueque · Docente: Juan León S.

CampusGPS obtiene la ubicación del celular, la muestra en un mapa y te dice qué lugar del campus tienes más cerca. Muestra cinco puntos ficticios (biblioteca, cafetería, laboratorio, auditorio y estacionamiento) con su distancia en metros, ordenados del más cercano al más lejano. La app sigue funcionando si el usuario niega el permiso, si el GPS está apagado o si el dispositivo no tiene Google Play Services.

## Tecnologías

| Elemento | Versión / detalle |
|---|---|
| Lenguaje | Kotlin 2.4.10 |
| UI | Jetpack Compose + Material 3 (sin XML de layouts) |
| Arquitectura | MVVM + Repository (interfaces) |
| Ubicación | `play-services-location:21.3.0` (FusedLocationProviderClient) |
| Corrutinas para `await()` | `kotlinx-coroutines-play-services:1.8.1` |
| Mapa | `maps-compose:8.4.0` |
| ViewModel en Compose | `lifecycle-viewmodel-compose:2.8.7` |
| Build | AGP 9.3.3, Gradle 9.5.0, compileSdk 37, minSdk 24 |
| Paquete | `com.alburquequearzapalo.campusgps` (namespace = applicationId) |

> Nota: `maps-compose:8.4.0` está compilado con Kotlin 2.4, por eso el plugin de Kotlin está en 2.4.10 (con 2.2.x el proyecto no compila).

## Estructura de paquetes

```
com.alburquequearzapalo.campusgps/
├── MainActivity.kt                 crea el ViewModel con la fábrica y monta PantallaMapa
├── model/
│   └── Modelos.kt                  Posicion, Lugar, LugarConDistancia, UbicacionUi, TipoError
├── data/
│   ├── UbicacionRepository.kt      interface (obtenerPosicion, playServicesDisponible)
│   ├── UbicacionRepositoryGoogle.kt implementación con FusedLocationProviderClient
│   ├── LugaresRepository.kt        interface
│   └── LugaresRepositoryLocal.kt   los 5 lugares ficticios
├── util/
│   ├── Distancia.kt                distanciaMetros() con Location.distanceBetween
│   └── Formato.kt                  formatearDistancia(): "350 m" / "1.5 km"
├── viewmodel/
│   ├── MapaViewModel.kt            estado, lugares, seleccionado, masCercano
│   └── CampusViewModelFactory.kt   construye el ViewModel con los repositorios
└── ui/
    ├── PantallaMapa.kt             pantalla principal y pedido de permisos
    ├── MapaUbicacion.kt            GoogleMap, cámara y marcadores
    ├── ListaLugares.kt             LazyColumn de tarjetas
    ├── MensajeError.kt             recuadro de error con su acción
    ├── FiltroCategorias.kt         chips de categoría (Fase 2)
    └── theme/Theme.kt              tema Material 3
```

## Arquitectura MVVM + Repository

```
View (PantallaMapa, MapaUbicacion, ListaLugares, MensajeError)
   |  evento: pulsar botón / tocar tarjeta       ^  collectAsState()
   v                                             |
ViewModel (MapaViewModel) ----- StateFlow -------+
   |  solo conoce interfaces (sin Context)
   v
UbicacionRepository ---> UbicacionRepositoryGoogle (FusedLocationProviderClient)
LugaresRepository   ---> LugaresRepositoryLocal (5 lugares fijos)

util/ (Distancia, Formato): funciones puras que usa el ViewModel
```

- La **View** solo habla con su ViewModel y dibuja según el estado. No calcula distancias.
- El **ViewModel** recibe interfaces de repositorio y no recibe `Context`. El `Context` solo se usa en `CampusViewModelFactory` para construir `UbicacionRepositoryGoogle`.
- El **Repository** es el único lugar que conoce Google Play Services.

## Configuración de la clave de Google Maps

1. En Google Cloud, crea una clave con **Maps SDK for Android** habilitado y restríngela al paquete `com.alburquequearzapalo.campusgps` y a tu SHA‑1 (`./gradlew signingReport`).
2. Pégala en `local.properties` (en la raíz del proyecto; este archivo **no** se sube a Git):
   ```properties
   MAPS_API_KEY=PEGA_AQUI_LA_CLAVE
   ```
3. Sincroniza Gradle. `app/build.gradle.kts` la lee y la pasa al Manifest con `manifestPlaceholders["MAPS_API_KEY"]`.

Sin clave la app abre igual: la ubicación, las distancias y la lista funcionan, pero el mapa se ve gris. Si Google Cloud pide activar facturación, el laboratorio indica consultarlo con el docente antes de seguir.

## Cómo ejecutar

**Emulador:** usa una imagen **con Google Play**. Ve a Extended controls → Location, escribe `-12.0464, -77.0428` → Set location. Luego pulsa *Obtener mi ubicación*.

**Teléfono Android por USB:**
1. En el teléfono, ve a Ajustes → Acerca del teléfono y toca 7 veces *Número de compilación*. Luego ve a Opciones de desarrollador y activa **Depuración USB**.
2. Conecta el cable, acepta el aviso *¿Permitir depuración USB?* y verifica:
   ```
   D:\AndroidSDK\platform-tools\adb.exe devices
   ```
3. Instala y abre la app desde Android Studio (Run ▶) o desde la terminal:
   ```
   .\gradlew installDebug
   ```
4. En un teléfono real se usa tu ubicación verdadera. Si no estás cerca de las coordenadas ficticias de Lima, las distancias saldrán en km, pero el orden y el lugar más cercano siguen calculándose bien.

## Capturas (pendientes)

Se agregarán tomadas del emulador o teléfono real. No se usan imágenes inventadas.

| # | Pantalla | Archivo |
|---|---|---|
| 1 | Cuadro de permiso | `docs/capturas/01-permiso.png` *(pendiente)* |
| 2 | Mapa con mi ubicación (*Estás aquí*) | `docs/capturas/02-mapa-ubicacion.png` *(pendiente)* |
| 3 | Marcadores de los 5 lugares | `docs/capturas/03-marcadores.png` *(pendiente)* |
| 4 | Lista ordenada por distancia | `docs/capturas/04-lista-ordenada.png` *(pendiente)* |
| 5 | Estado de error (permiso denegado / GPS apagado) | `docs/capturas/05-error.png` *(pendiente)* |

## Pruebas

**Pruebas unitarias (JUnit):** `FormatoTest` comprueba metros enteros y km con un decimal. Se ejecutan con `.\gradlew testDebugUnitTest`.

**Pruebas de aceptación** (se marcan al ejecutarlas en el emulador o el teléfono):

| Prueba | Resultado esperado | OK |
|---|---|---|
| Abrir la app por primera vez y pulsar el botón | Aparece el cuadro de permiso | ☐ |
| Aceptar el permiso | Se ven latitud, longitud y el marcador *Estás aquí* | ☐ |
| Negar el permiso | Mensaje de error con *Abrir ajustes*, la app no se cierra | ☐ |
| Ubicación en el punto base | Los 5 marcadores azules aparecen cerca de tu posición | ☐ |
| Tocar un marcador | Se ve el nombre y la categoría del lugar | ☐ |
| Lista de lugares | Ordenada de menor a mayor distancia, con m o km | ☐ |
| Tocar una tarjeta de la lista | La cámara viaja a ese lugar y la tarjeta queda resaltada | ☐ |
| Ir al más cercano | La cámara va al lugar verde; el botón está deshabilitado antes de buscar | ☐ |
| Cambiar la ubicación y buscar otra vez | Las distancias, el orden y el lugar verde se actualizan | ☐ |
| Ubicación apagada | Mensaje de GPS con *Reintentar* | ☐ |
| Rotar la pantalla | No se pierden la ubicación ni la lista | ☐ |
| Revisar el repositorio | buscar la clave con `git grep` no encuentra nada y `local.properties` no está subido | ☐ |

## Para pensar

**1. ¿Por qué FusedLocationProviderClient está dentro del Repository y no en el ViewModel?**
Porque el ViewModel solo debe tener la lógica de la pantalla y no depender de Android ni de Google Play Services. `FusedLocationProviderClient` necesita un `Context`, y si lo guardáramos en el ViewModel podríamos tener fugas de memoria (el ViewModel vive más que la Activity). Además, al depender solo de la interfaz `UbicacionRepository`, se puede cambiar la implementación (por ejemplo, por un repositorio falso para pruebas) sin tocar el ViewModel.

**2. ¿Qué diferencia hay entre el permiso de ubicación y la clave de API de Maps? ¿Qué pasa si falta cada uno?**
El permiso lo concede el **usuario** en tiempo de ejecución y autoriza a la app a leer la ubicación del dispositivo. La clave de API identifica a la **app ante Google** para usar el servicio de mapas. Sin permiso, no se puede obtener la posición: la app muestra "Sin permiso de ubicación…" con *Abrir ajustes*, pero el mapa y la lista se pueden ver. Sin clave, la ubicación y las distancias funcionan, pero el mapa sale gris o en blanco (*Authorization failure* en Logcat).

**3. ¿Por qué la clave va en local.properties y no en el código? ¿Qué harías si se subiera a GitHub por error?**
`local.properties` está en `.gitignore`, así que la clave se queda en mi computadora y llega al Manifest al compilar con `manifestPlaceholders`. Si estuviera en el código, cualquiera que vea el repositorio podría copiarla y usar mi cuota. Si se subiera por error, la **borraría en Google Cloud y crearía una nueva** (aunque borre el commit, la clave ya quedó expuesta en el historial), y revisaría el `.gitignore`.

**4. ¿Dónde calculas la distancia y por qué no en el Composable?**
En el ViewModel (`actualizarDistancias`), usando la función pura `distanciaMetros` de `util/Distancia.kt` con `Location.distanceBetween`. Solo se calcula una vez cuando llega la ubicación. Un Composable se recompone muchas veces, así que calcular ahí repetiría el trabajo y mezclaría lógica con la vista, lo que rompe MVVM. La pantalla solo formatea un valor ya calculado.

**5. ¿Qué pasa con tu app si el usuario niega el permiso dos veces? ¿Cómo lo manejas?**
Desde Android 11, después de dos negaciones el sistema ya no vuelve a mostrar el cuadro de permiso: el launcher responde "denegado" de inmediato. La app no se cierra: `vm.permisoDenegado()` muestra el mensaje *Sin permiso de ubicación no puedo mostrar dónde estás* y el botón **Abrir ajustes**, que abre la pantalla de la app (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`), donde el usuario puede activar el permiso manualmente.

## Commits de la Fase 1 (rama `main`)

1. `chore: importa proyecto base CampusGPS`
2. `chore: configura permisos y clave de Maps con local.properties`
3. `feat(ubicacion): pide permiso de ubicacion en tiempo de ejecucion`
4. `feat(mapa): muestra mapa con marcador de la ubicacion actual`
5. `feat(mapa): agrega marcadores de los lugares del campus`
6. `feat(distancia): calcula y ordena lugares por distancia`
7. `feat(ui): agrega lista de lugares y seleccion en el mapa`
8. `feat(ui): resalta y navega al lugar mas cercano`
9. `feat(errores): maneja permiso denegado, GPS apagado y sin Play Services`
10. `docs: agrega README con estructura y capturas`

## Fase 2 – Mejoras con IA (rama `mejora-ia`)

| Mejora | Qué hace | Capas que cambian |
|---|---|---|
| Punto azul nativo | Activa `isMyLocationEnabled` y el botón de mi ubicación del mapa solo si el permiso fue concedido | View |
| Filtrar por categoría | Chips *Todos / Estudio / Comida / Eventos / Servicios*: filtran el mapa y la lista; el más cercano se calcula sobre lo visible | ViewModel + View (`FiltroCategorias.kt`) |
| Seguimiento en vivo | `requestLocationUpdates` convertido en `Flow` con `callbackFlow`, botón *Iniciar/Detener seguimiento* | Repository + ViewModel + View |

Pruebas unitarias en esta rama: `FormatoTest` (2) y `MapaViewModelTest` (8, con un repositorio falso). El análisis de los prompts está en [`PROMPTS.md`](PROMPTS.md).

## Uso de IA

El docente autorizó usar IA también en la Fase 1. El código de este repositorio se desarrolló con ayuda de **Claude Code** (asistente de IA de Anthropic), siguiendo los contratos del PDF del laboratorio. No se tuvo el proyecto base del docente, así que el commit 1 reconstruye el "ejemplo resuelto" de la sección 8 del PDF. El detalle de los prompts y de las decisiones está en [`PROMPTS.md`](PROMPTS.md).
