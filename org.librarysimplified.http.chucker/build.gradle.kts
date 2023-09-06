dependencies {
    implementation(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat.resources)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.constraintlayout.core)
    implementation(libs.androidx.constraintlayout.solver)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.recycler.view)
    implementation(libs.chucker)
    implementation(libs.google.material)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
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
