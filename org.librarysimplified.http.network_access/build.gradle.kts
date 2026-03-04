dependencies {
    coreLibraryDesugaring(libs.android.desugaring)

    implementation(project(":org.librarysimplified.http.api"))

    implementation(libs.io7m.jattribute.core)
    implementation(libs.slf4j)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        val versionName = project.extra["VERSION_NAME"] as String
        buildConfigField("String", "NETWORK_ACCESS_VERSION_NAME", "\"${versionName}\"")
    }
}
