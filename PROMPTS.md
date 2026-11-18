# PROMPTS.md – Uso de IA en CampusGPS

**Herramienta usada:** Claude Code (asistente de IA de Anthropic), trabajando directamente sobre los archivos del proyecto.
El PDF propone Gemini en Android Studio; en este proyecto se usó Claude Code. El docente autorizó usar IA también en la Fase 1.
**Nunca se pegó una clave de API en un prompt.** El proyecto no contiene ninguna clave: se lee de `local.properties`, que está en `.gitignore`.

Este archivo registra los prompts importantes, lo que hizo la IA y las correcciones que se hicieron durante el trabajo. La columna *Revisión de la autora* queda para completar después de probar la app.

## Fase 1 (rama `main`)

| Prompt que escribí | Qué respondió / hizo la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| «Quiero que DESARROLLES Y COMPLETES mi proyecto CampusGPS… Kotlin, Compose, Material 3, MVVM + Repository, Play Services Location, Maps Compose, paquete `com.alburquequearzapalo.campusgps`…» | Revisó el entorno (Android SDK en `D:\AndroidSDK`, JBR de Android Studio, Gradle). Creó una primera versión completa con su propia estructura (`CampusViewModel`, `FusedLocationDataSource`, Haversine, lista desplazable). Compiló sin errores. | Esa versión **se descartó** para la entrega (quedó solo en el respaldo local `D:\CampusGPS-respaldo`). | Funcionaba, pero no seguía los nombres de archivo, las firmas, el diseño ni los 10 commits del laboratorio. Eso hace perder puntos de MVVM y de Git. |
| «Te adjunto el PDF original del Laboratorio 13… compara sus instrucciones con lo que ya implementaste…» | Leyó el PDF y listó lo que se cumplía, lo que faltaba y los cambios necesarios: Modelos/UbicacionUi/TipoError, `Location.distanceBetween` en lugar de Haversine, km con 1 decimal, colores de marcador (rojo propio, azul lugares, verde el más cercano), botón *Obtener mi ubicación*, mensajes de error exactos. | Se decidió **mantener el paquete `com.alburquequearzapalo.campusgps`** en lugar de `com.tecsup.campusgps`. El punto azul nativo y el círculo de precisión **se sacaron de la Fase 1**. | El paquete fue una instrucción explícita. El punto azul es una mejora de la Fase 2 en el PDF. |
| «Haz una copia de seguridad… reconstruye `main` con los 10 commits reales del laboratorio… solo dos ramas…» | Creó el respaldo completo. Reinició `main` sin crear ramas extra y reconstruyó los 10 pasos uno por uno, compilando antes de cada commit. | Ver correcciones abajo. | Un commit por paso, con el mensaje indicado. |

### Correcciones reales durante la Fase 1

1. **Kotlin incompatible:** con Kotlin 2.2.10, `maps-compose:8.4.0` (la versión del PDF) no compilaba (*metadata 2.4.0, expected 2.2.0*). Se subió el plugin de Kotlin a **2.4.10**.
2. **`java.util.Properties` en `build.gradle.kts`:** dentro del bloque Android, `java` apunta a la extensión de Gradle y no al paquete, así que el script no compilaba. Se corrigió con `import java.util.Properties`. Ese commit se había creado con el build fallando porque el script de verificación no cortaba ante errores; se corrigió el script y se enmendó el commit 2 antes de subir nada.
3. **`sdk.dir` en `local.properties`:** la ruta con barras invertidas mal escapadas daba *Invalid file path*. Se usó `D:/AndroidSDK`.
4. **Clave vacía:** si `MAPS_API_KEY` no existe, se usa el texto de relleno `SIN_CLAVE` para que la app no se cierre (el mapa sale gris).
5. **Prueba de búsqueda de la clave con `git grep`:** el ejemplo del README contenía el mismo texto que busca la prueba, así que habría fallado. Se cambió el ejemplo.
6. **MVVM:** el `Context` solo se usa en `CampusViewModelFactory` y en `UbicacionRepositoryGoogle`, nunca en `MapaViewModel`. Las distancias se calculan en el ViewModel con `util/Distancia.kt`; la pantalla solo formatea.

## Fase 2 (rama `mejora-ia`)

Prompt: «Cuando termines y verifiques `main`, crea `mejora-ia` para la Fase 2, con mínimo 3 commits reales.» La IA eligió tres mejoras de la tabla del PDF. Dos de ellas cambian el ViewModel o el Repository.

| Mejora | Qué propuso / hizo la IA | Decisión técnica (y qué se evitó) | Capas |
|---|---|---|---|
| **Punto azul nativo** | `MapProperties(isMyLocationEnabled = tienePermiso)` y `MapUiSettings(myLocationButtonEnabled = tienePermiso)`. | Solo se activa si hay permiso: activarlo sin permiso lanza `SecurityException`. El permiso se revisa en la View con `remember(estado)`, así se vuelve a evaluar al cambiar el estado, sin meter `Context` en el ViewModel. | View |
| **Filtrar por categoría** | `categoria`, `categorias` y `lugaresVisibles = combine(_lugares, _categoria).stateIn(...)` en el ViewModel. Chips en `FiltroCategorias.kt`. | El filtrado está en el ViewModel, no en el Composable. `masCercano` ahora se deriva de los lugares visibles, para que el verde coincida con lo que se ve. Si el lugar seleccionado queda oculto, se limpia la selección. Se agregaron pruebas JVM con un repositorio falso. | ViewModel + View |
| **Seguimiento en vivo** | `seguirPosicion(): Flow<Posicion>` en la interfaz. En `UbicacionRepositoryGoogle`, `requestLocationUpdates` dentro de `callbackFlow` con `awaitClose { removeLocationUpdates }`. En el ViewModel, un `Job` cancelable y `siguiendo: StateFlow<Boolean>`. Botón Iniciar/Detener. | `awaitClose` evita que el GPS siga encendido al detener. El `Job` vive en `viewModelScope` (sobrevive a la rotación y se cancela al cerrar la pantalla). **Corrección:** el `onCompletion` del trabajo anterior podía apagar un seguimiento recién reiniciado; ahora solo apaga el indicador si es el trabajo vigente. | Repository + ViewModel + View |

### Verificación de la Fase 2
- `.\gradlew clean assembleDebug testDebugUnitTest`: compila y pasan 10 pruebas unitarias (`FormatoTest` 2, `MapaViewModelTest` 8).
- Pendiente: probar las tres mejoras en un emulador con Google Play o en un teléfono real.

## Revisión de la autora (completar)

| Mejora | ¿La probé? | ¿Qué cambiaría? | Explicación con mis palabras |
|---|---|---|---|
| Punto azul nativo | ☐ | | |
| Filtrar por categoría | ☐ | | |
| Seguimiento en vivo | ☐ | | |
