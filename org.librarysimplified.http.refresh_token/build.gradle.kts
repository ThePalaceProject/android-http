dependencies {
    api(project(":org.librarysimplified.http.api"))
    api(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.jackson.databind)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.okhttp3)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "REFRESH_TOKEN_VERSION_NAME", "\"${versionName}\"")
    }
}
