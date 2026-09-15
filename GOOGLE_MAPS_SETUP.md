# Configurar la API Key de Google Maps

El mapa de seguimiento en vivo (`SeguimientoMapaScreen`, para servicios de Taxi/Paseo) usa
Google Maps SDK for Android. Sin una API key real, el proyecto **compila y corre
normalmente**, pero el mapa se ve con el watermark "for development purposes only" y tiles
grises en vez de imágenes reales — es el comportamiento esperado del SDK sin key válida, no
un bug.

## Obtener una API key

1. Ir a [Google Cloud Console](https://console.cloud.google.com/) e iniciar sesión.
2. Crear un proyecto nuevo (o seleccionar uno existente) — por ejemplo "PetCare Maps".
3. Ir a **APIs y servicios → Biblioteca**, buscar **"Maps SDK for Android"** y habilitarlo.
4. Ir a **APIs y servicios → Credenciales → Crear credenciales → Clave de API**.
5. Restringir la key (recomendado, evita uso indebido si se filtra):
   - **Restricciones de aplicaciones** → "Aplicaciones para Android" → agregar:
     - Nombre del paquete: `com.proyectopoo.petcareapp`
     - Huella SHA-1: obtenerla con `./gradlew signingReport` (busca la línea `SHA1` bajo
       `Variant: debug`; repetir para `release` si vas a firmar con un keystore real).
   - **Restricciones de API** → "Restringir clave" → seleccionar solo "Maps SDK for Android".
6. Copiar la key generada (empieza con `AIza...`).

## Configurarla en el proyecto

1. Copiar `secrets.properties.example` a `secrets.properties` (en la raíz del repo, ya está
   en `.gitignore` — nunca se sube a Git):
   ```
   MAPS_API_KEY=AIzaSy...tu_clave_aqui
   ```
2. Sincronizar Gradle. `app/build.gradle.kts` lee `secrets.properties` y la inyecta como
   `manifestPlaceholders["mapsApiKey"]`, que `AndroidManifest.xml` usa en el
   `<meta-data android:name="com.google.android.geo.API_KEY">` del SDK.
3. Recompilar (`./gradlew assembleDebug`) y correr la app — el mapa debería verse con
   imágenes reales en vez del watermark.

## Nota sobre el mecanismo usado

Este proyecto lee `secrets.properties` manualmente en `app/build.gradle.kts` (mismo patrón
que ya usa `BASE_URL`), en vez del plugin oficial
`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`. El resultado final es
idéntico — una key gitignored inyectada vía `manifestPlaceholders` — pero evita una
dependencia de plugin adicional para algo que ya funciona con las herramientas que el
proyecto ya usa. Si prefieres el plugin oficial (por ejemplo, por su soporte de
`local.defaults.properties` como fallback por variante), es un cambio pequeño y aislado a
`build.gradle.kts` / `app/build.gradle.kts` — avisa y se agrega.
