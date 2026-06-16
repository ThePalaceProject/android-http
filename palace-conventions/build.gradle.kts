plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:9.2.1")
}

gradlePlugin {
    plugins {
        register("PalaceAAR") {
            id = "org.thepalaceproject.build.aar"
            implementationClass = "PalaceAAR"
        }
        register("PalaceKtLint") {
            id = "org.thepalaceproject.ktlint"
            implementationClass = "PalaceKtLint"
        }
    }
}
