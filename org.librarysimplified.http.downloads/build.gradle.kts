dependencies {
    implementation(project(":org.librarysimplified.http.api"))

    implementation(libs.commons.compress)
    implementation(libs.irradia.mime.api)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.joda.time)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "DOWNLOADS_VERSION_NAME", "\"${versionName}\"")
    }
}
