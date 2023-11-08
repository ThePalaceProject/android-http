dependencies {
    val dependencyObjects = setOf(
        project(":org.librarysimplified.http.api"),
        project(":org.librarysimplified.http.bearer_token"),
        project(":org.librarysimplified.http.downloads"),
        project(":org.librarysimplified.http.refresh_token"),
        project(":org.librarysimplified.http.uri_builder"),
        project(":org.librarysimplified.http.vanilla"),

        libs.bouncycastle,
        libs.bouncycastle.bcprov,
        libs.bouncycastle.pki,
        libs.bouncycastle.tls,
        libs.bytebuddy,
        libs.bytebuddy.agent,
        libs.commons.compress,
        libs.irradia.mime.api,
        libs.irradia.mime.vanilla,
        libs.jackson.annotations,
        libs.jackson.core,
        libs.jackson.databind,
        libs.joda.time,
        libs.junit,
        libs.junit.jupiter.api,
        libs.junit.jupiter.engine,
        libs.junit.jupiter.vintage,
        libs.junit.platform.commons,
        libs.junit.platform.engine,
        libs.kotlin.reflect,
        libs.kotlin.stdlib,
        libs.logback.core,
        libs.logback.classic,
        libs.mockito.core,
        libs.mockito.kotlin,
        libs.mockwebserver,
        libs.objenesis,
        libs.okhttp3,
        libs.okio,
        libs.opentest,
        libs.ow2,
        libs.ow2.asm,
        libs.ow2.asm.commons,
        libs.ow2.asm.tree,
        libs.slf4j,
    )

    for (dep in dependencyObjects) {
        implementation(dep)
        testImplementation(dep)
    }
}

afterEvaluate {
    tasks.matching { task -> task.name.contains("UnitTest") }
        .forEach { task -> task.enabled = true }
}
