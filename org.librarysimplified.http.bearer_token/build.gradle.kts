dependencies {
    coreLibraryDesugaring(libs.android.desugaring)

    implementation(project(":org.librarysimplified.http.api"))
    implementation(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.jackson.annotations)
    implementation(libs.jackson.core)
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
        buildConfigField("String", "BEARER_TOKEN_VERSION_NAME", "\"${versionName}\"")
    }
}
