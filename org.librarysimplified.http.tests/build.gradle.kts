dependencies {
    implementation(project(":org.librarysimplified.http.api"))
    implementation(project(":org.librarysimplified.http.bearer_token"))
    implementation(project(":org.librarysimplified.http.downloads"))
    implementation(project(":org.librarysimplified.http.uri_builder"))
    implementation(project(":org.librarysimplified.http.vanilla"))

    implementation(libs.bouncycastle)
    implementation(libs.bouncycastle.pki)
    implementation(libs.bouncycastle.tls)
    implementation(libs.joda.time)
    implementation(libs.junit.jupiter.api)
    implementation(libs.junit.jupiter.engine)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib)
    implementation(libs.logback.classic)
    implementation(libs.mockito.android)
    implementation(libs.mockito.kotlin)
    implementation(libs.mockwebserver)
    implementation(libs.slf4j)
}