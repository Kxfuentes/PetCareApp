# PetCareApp

![Android CI](https://github.com/Kxfuentes/PetCareApp/actions/workflows/build.yml/badge.svg)

Aplicación móvil desarrollada en Android Studio con Kotlin y Jetpack Compose como proyecto universitario de Programación Orientada a Objetos.

## Descripción
PetCareApp es una app estilo “niñera de mascotas” donde usuarios pueden registrarse como dueños o cuidadores, visualizar solicitudes de servicios para mascotas y publicar nuevas solicitudes.

## Funcionalidades implementadas (v2.0)
* Login/registro de usuario, selección de rol (Dueño / Cuidador)
* Feed de servicios disponibles, creación y gestión de solicitudes de servicio
* Perfil de usuario y de mascota
* Chat interno con fotos y confirmación de lectura
* Seguimiento en vivo por mapa (Google Maps) para Taxi/Paseo
* Botón de emergencia, compartir solicitud, valoración en tiempo real
* Botón de llamada telefónica directa
* Etiquetas de usuario por calificación (badges)
* Foto obligatoria antes/después de un servicio (con cola offline)
* Calendario integrado (vista mensual y semanal)
* Expediente médico de la mascota con alertas de vacunas próximas
* Alerta de mascota perdida con notificación a usuarios cercanos y avistamientos
* Caché offline de chat, accesibilidad (WCAG AA), soporte para inglés
* Navegación completa entre pantallas

## Tecnologías utilizadas
* Kotlin
* Jetpack Compose
* Navigation Compose
* Material 3
* Git & GitHub

## Instrucciones para ejecutar
1. Clonar el repositorio:
   ```
   git clone URL_DE_ESTE_REPOSITORIO
   ```
2. Copiar `secrets.properties.example` a `secrets.properties` en la raíz del repo (ya está en
   `.gitignore`). Sin una API Key real de Google Maps, la app compila y corre normal, pero el
   mapa de seguimiento se ve con el watermark "for development purposes only" — ver
   [`GOOGLE_MAPS_SETUP.md`](GOOGLE_MAPS_SETUP.md).
3. Abrir el proyecto en Android Studio y sincronizar Gradle, o desde la línea de comandos:
   ```powershell
   ./gradlew assembleDebug
   ```
4. Ejecutar en un emulador o dispositivo físico (▶ en Android Studio, o `./gradlew installDebug`
   con un emulador/dispositivo ya conectado).
5. `BASE_URL` apunta por defecto a `http://10.0.2.2:8080/` (el backend corriendo en `localhost`
   desde el emulador). Si usas un dispositivo físico o el backend está en otra máquina, pásalo
   como propiedad de Gradle: `./gradlew installDebug -PBASE_URL=http://TU_IP:8080/`.

### Solución de problemas conocidos
- **La app crashea al abrir / al terminar el onboarding** (`IllegalArgumentException` sobre
  `NavType` o "Navigation destination ... cannot be found"): estos dos bugs ya se corrigieron
  (rutas `Double` sin `NavType` en navegación type-safe, y una navegación con ruta de texto
  (`navigate("Login")`) en vez de la ruta tipada (`navigate(Login)`) — si ves un error parecido
  en una pantalla nueva, es casi siempre una de estas dos causas.

## Tests
El módulo `app` incluye tests unitarios (JVM, sin emulador) bajo `app/src/test/`.

Además de utilidades como filtros y cálculo de distancias, hay tests de la capa de red en
`app/src/test/java/com/proyectopoo/petcareapp/data/network/ApiServiceMockWebServerTest.kt`
que usan [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) para
levantar un servidor HTTP local falso y verificar, contra un par de endpoints de `ApiService`
(`getChatMessages`, `sendChatMessage`), que la app maneja correctamente respuestas 200,
errores HTTP (400/500, expuestos como `Response<T>` no exitoso en vez de una excepción) y
timeouts de red (propagados como `IOException`, que es lo que `RetryInterceptor` reintenta
en peticiones GET).

Para ejecutar todos los tests unitarios:
```
./gradlew testDebugUnitTest
```

## Gestión de ramas
Cada integrante trabajó en su propia rama para mantener organización y control de versiones.

## Integrantes
* Kelly Xaviera Fuentes Areas: Infraestructura, navegación y GitHub
* Ariana Vanessa Àvila Fajardo: Login, roles y perfil
* José Nahúm Espinoza Solano: Feed y creación de servicios
