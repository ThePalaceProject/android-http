dependencies {
    implementation(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.chucker)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.okhttp3)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "CHUCKER_VERSION_NAME", "\"${versionName}\"")
    }
}
