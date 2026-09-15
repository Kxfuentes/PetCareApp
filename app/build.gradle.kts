import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// --- Firebase Cloud Messaging (FCM) ---
// El plugin `com.google.gms.google-services` lee `app/google-services.json` para generar
// los recursos que Firebase necesita para inicializarse (project id, api key, app id, etc).
// Ese archivo TODAVIA NO EXISTE en este repo (no hay proyecto Firebase creado aun), y
// aplicar el plugin sin el archivo rompe el build para cualquiera que lo clone.
//
// Por eso el plugin solo se aplica condicionalmente, cuando el archivo esta presente.
// Sin el archivo: el modulo compila normalmente, la dependencia de firebase-messaging
// esta disponible, pero FirebaseApp nunca se inicializa con credenciales reales, por lo
// que PetCareFirebaseMessagingService jamas es invocado por el sistema (no hay push real).
//
// Para activar FCM de verdad:
//   1. Crear un proyecto en https://console.firebase.google.com
//   2. Registrar la app con applicationId "com.proyectopoo.petcareapp"
//   3. Descargar el `google-services.json` generado y copiarlo a `app/google-services.json`
//   4. Sincronizar Gradle (este bloque detectara el archivo y aplicara el plugin solo)
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// --- Google Maps (seguimiento en vivo de Taxi/Paseo) ---
// Igual que con Firebase arriba: este repo NO trae una API key real de Google Maps (no hay
// proyecto de Google Cloud configurado). En vez de depender del plugin oficial
// "secrets-gradle-plugin", leemos la key directamente de `secrets.properties` (raiz del repo,
// gitignored) con el mismo patron que BASE_URL usa mas abajo con gradle.properties.
//
// Sin `secrets.properties` (o sin MAPS_API_KEY dentro): el proyecto compila y corre normal,
// pero el mapa embebido de seguimiento se ve con el watermark "for development purposes only"
// y tiles grises en vez de imagenes reales — es el comportamiento esperado del SDK sin key
// valida, no un bug. Para activarlo de verdad:
//   1. Crear un proyecto en https://console.cloud.google.com y habilitar "Maps SDK for Android"
//   2. Generar una API key y copiarla a `secrets.properties` como MAPS_API_KEY=...
//   3. Sincronizar Gradle (se inyecta via manifestPlaceholders, ver AndroidManifest.xml)
val secretsProperties = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { stream -> load(stream) }
    }
}

android {
    namespace = "com.proyectopoo.petcareapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.proyectopoo.petcareapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Manejamos la URL base como una variable que puede venir del sistema
        // Si no se define BASE_URL en el sistema o gradle.properties, usa la del emulador por defecto
        val baseUrl = project.findProperty("BASE_URL")?.toString()
            ?: System.getenv("BASE_URL")
            ?: "http://10.0.2.2:8080/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        // Ver comentario de "Google Maps" arriba: vacio por defecto si no hay secrets.properties.
        val mapsApiKey = secretsProperties.getProperty("MAPS_API_KEY")?.toString().orEmpty()
        manifestPlaceholders["mapsApiKey"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        // local.properties is per-machine and gitignored — never committed — so lint
        // complaining about its escaping on one developer's machine can't be "fixed" in
        // source control, and the format Android Studio itself generates on Windows is
        // what this check flags.
        disable += "PropertyEscape"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // Explicit, current version: without this, a transitive Fragment dependency below
    // 1.3.0 gets pulled in, which lint flags as unsafe for the ActivityResult APIs used
    // in MainActivity (registerForActivityResult).
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.okhttp.logging)

    // Coil (carga de imágenes remotas)
    implementation(libs.coil.compose)

    // Google Maps (mapa embebido de seguimiento en vivo) y Fused Location (GPS del cuidador).
    // Versiones fijadas deliberadamente por debajo de la última estable: las versiones más
    // recientes de maps-compose y play-services-location vienen compiladas con metadata de
    // Kotlin 2.1+/2.2+, que el compilador Kotlin 2.0.21 de este proyecto no puede leer
    // ("Module was compiled with an incompatible version of Kotlin"). Si se sube el plugin de
    // Kotlin de este proyecto más adelante, estas dos se pueden actualizar también.
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Firebase Cloud Messaging. Compila sin google-services.json; solo requiere el archivo
    // (y el plugin aplicado arriba) para inicializarse con credenciales reales en runtime.
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.compose.material3.lint)
    implementation(libs.androidx.ui)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
