# Contexto del Proyecto: MyTracker 📚

## Descripción General
MyTracker es una aplicación Android profesional para rastrear el progreso de lectura de Manga y visionado de Anime.

## Stack Tecnológico
- **Lenguaje**: Kotlin
- **Framework UI**: Jetpack Compose con Material 3
- **Arquitectura**: MVVM + Repository Pattern
- **Base de Datos**: Firebase Firestore (Tiempo real)
- **Redes**: Retrofit 2 + GSON
- **Imágenes**: Coil para carga asíncrona
- **Navegación**: Jetpack Compose Navigation

## Estructura de Archivos Clave
- `MainActivity.kt`: Punto de entrada, define la navegación (`AppNavigation`) y las pantallas principales (`TrackerScreen`, `DetailScreen`, `SettingsScreen`).
- `TrackerViewModel.kt`: Gestiona el estado de la UI y la comunicación con el repositorio.
- `TrackerRepository.kt`: Fuente única de verdad. Maneja las consultas a Firestore y las llamadas a la Jikan API.
- `JikanApiService.kt`: Definición de los endpoints de la API de MyAnimeList.
- `TrackItem.kt`: Modelo de datos principal (id, título, capítulo actual, estado, URL de imagen, capítulos totales, géneros).

## Reglas y Convenciones del Proyecto
1. **Flujo de Datos**: Siempre usar `Flow` o `StateFlow` para observar cambios en tiempo real desde Firestore a través del Repositorio.
2. **Actualizaciones de Progreso**: El sistema incrementa capítulos de uno en uno. Si el capítulo actual iguala al `totalChapters`, el estado debe cambiar automáticamente a "Completed".
3. **Búsqueda**: La búsqueda en la API Jikan requiere al menos 3 caracteres para disparar la consulta.
4. **Diseño**: Usar componentes de Material 3 y soportar Modo Oscuro (implementado manualmente y por sistema).

## Hoja de Ruta Futura
- Implementar Inyección de Dependencias con **Hilt**.
- Añadir soporte offline con **Room Database** (Caché local).
- Integrar Autenticación de Usuarios con **Firebase Auth**.
- Soporte para Tematizado Dinámico (Material You).
