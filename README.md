# PetCareApp

Aplicación móvil desarrollada en Android Studio con Kotlin y Jetpack Compose como proyecto universitario de Programación Orientada a Objetos.

## Descripción
PetCareApp es una app estilo “niñera de mascotas” donde usuarios pueden registrarse como dueños o cuidadores, visualizar solicitudes de servicios para mascotas y publicar nuevas solicitudes.

##  Funcionalidades implementadas (Primer avance)
* Login de usuario
* Selección de rol (Dueño / Cuidador)
* Feed de servicios disponibles
* Creación de solicitudes de servicio
* Perfil de usuario
* Navegación completa entre pantallas

## Tecnologías utilizadas
* Kotlin
* Jetpack Compose
* Navigation Compose
* Material 3
* Git & GitHub

## Instrucciones para ejecutar
1. Clonar el repositorio:
   git clone URL_DE_ESTE_REPOSITORIO

2. Abrir el proyecto en Android Studio

3. Sincronizar Gradle

4. Ejecutar en emulador o dispositivo físico

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
