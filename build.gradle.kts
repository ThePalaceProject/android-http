import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    id("org.jetbrains.kotlin.jvm")
        .version("1.9.0")
        .apply(false)

    id("org.jetbrains.kotlin.android")
        .version("1.9.0")
        .apply(false)

    id("com.android.library")
        .version("8.1.0")
        .apply(false)

    id("maven-publish")
}

repositories {
    mavenCentral()
}

val deployDirectory = "$rootDir/build/maven"

fun property(
    project: Project,
    name: String
): String {
    return project.extra[name] as String
}

/*
 * Configure Maven publishing. Artifacts are published to a local directory
 * so that they can be pushed to Maven Central in one step using brooklime.
 */

fun configurePublishingFor(project: Project) {
    project.publishing {
        publications {
            create<MavenPublication>("MavenPublication") {
                groupId = property(project,"GROUP")
                artifactId = property(project,"POM_ARTIFACT_ID")
                version = property(project,"VERSION_NAME")

                pom {
                    name.set(property(project,"POM_NAME"))
                    description.set(property(project,"POM_DESCRIPTION"))
                    url.set(property(project,"POM_URL"))
                    scm {
                        connection.set(property(project,"POM_SCM_CONNECTION"))
                        developerConnection.set(property(project,"POM_SCM_DEV_CONNECTION"))
                        url.set(property(project,"POM_SCM_URL"))
                    }
                    licenses {
                        license {
                            name.set(property(project,"POM_LICENCE_NAME"))
                            url.set(property(project,"POM_LICENCE_URL"))
                        }
                    }
                }
                from(components["java"])
            }
        }

        repositories {
            maven {
                url = uri(deployDirectory)
            }
        }
    }
}

allprojects {

    /*
     * Configure builds and tests for various project types.
     */

    when (extra["POM_PACKAGING"]) {
        "pom" -> {
            logger.info("Configuring ${this.project} $version as a pom project")
        }

        "aar" -> {
            logger.info("Configuring ${this.project} $version as an aar project")

            apply(plugin = "com.android.library")
            apply(plugin = "org.jetbrains.kotlin.android")

            /*
             * Configure the JVM toolchain version that we want to use for Kotlin.
             */

            val kotlin : KotlinAndroidProjectExtension =
                this.extensions["kotlin"] as KotlinAndroidProjectExtension

            kotlin.jvmToolchain(17)

            /*
             * Configure the various required Android properties.
             */

            val android : LibraryExtension =
                this.extensions["android"] as LibraryExtension

            android.namespace = property(this, "POM_ARTIFACT_ID")
            android.compileSdk = 33
        }

        "jar" -> {
            logger.info("Configuring ${this.project} $version as a jar project")

            apply(plugin = "org.jetbrains.kotlin.jvm")

            /*
             * Configure JUnit tests.
             */

            tasks.named<Test>("test") {
                useJUnitPlatform()

                testLogging {
                    events("passed")
                }
            }
        }
    }

    /*
     * Configure publishing.
     */

    when (extra["POM_PACKAGING"]) {
        "jar" -> {
            apply(plugin = "maven-publish")
            configurePublishingFor(this.project)
        }

        "pom", "aar" -> {

        }
    }
}
