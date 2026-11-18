// Archivo de build raiz: solo declara los plugins que usa el modulo :app.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
