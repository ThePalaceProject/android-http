dependencies {
    coreLibraryDesugaring(libs.android.desugaring)

    compileOnly(libs.osgi.bundle.annotation)

    implementation(libs.irradia.mime.api)
    implementation(libs.jackson.core)
    implementation(libs.joda.time)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
}
