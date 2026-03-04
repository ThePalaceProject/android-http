dependencies {
    coreLibraryDesugaring(libs.android.desugaring)

    compileOnly(libs.osgi.bundle.annotation)

    implementation(libs.io7m.jattribute.core)
    implementation(libs.irradia.mime.api)
    implementation(libs.jackson.core)
    implementation(libs.joda.time)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.slf4j)
}
