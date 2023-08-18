import org.jetbrains.kotlin.gradle.plugin.extraProperties

dependencies {
    api(project(":org.librarysimplified.http.api"))

    implementation(libs.irradia.mime.api)
    implementation(libs.irradia.mime.vanilla)
    implementation(libs.jackson.databind)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.okhttp3)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "HTTP_VERSION_NAME", "\"${versionName}\"")
    }
}
