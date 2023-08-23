import org.jetbrains.kotlin.gradle.plugin.extraProperties

dependencies {
    api(project(":org.librarysimplified.http.api"))
    api(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.jackson.databind)
    implementation(libs.joda.time)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.okhttp3)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "OAUTH_CLIENT_CREDENTIALS_VERSION_NAME", "\"${versionName}\"")
    }
}
