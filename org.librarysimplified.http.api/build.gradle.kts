dependencies {
    compileOnly(libs.osgi.bundle.annotation)

    api(libs.irradia.mime.api)
    api(libs.jackson.core)
    api(libs.joda.time)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
}
