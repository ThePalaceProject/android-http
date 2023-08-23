dependencies {
    implementation(project(":org.librarysimplified.http.api"))

    implementation(libs.joda.time)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "DOWNLOADS_VERSION_NAME", "\"${versionName}\"")
    }
}
