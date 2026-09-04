// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Firebase Cloud Messaging (FCM): el plugin se declara aqui con "apply false" para que
    // este disponible en el classpath, pero solo se aplica de verdad en app/build.gradle.kts
    // y unicamente si existe app/google-services.json. Ver comentario en ese archivo.
    alias(libs.plugins.google.services) apply false
}
