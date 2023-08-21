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

    /*
     * Android Junit5 plugin. Required to run JUnit 5 tests on Android projects.
     *
     * https://github.com/mannodermaus/android-junit5
     */

    id("de.mannodermaus.android-junit5")
        .version("1.9.3.0")
        .apply(false)

    id("maven-publish")
}

repositories {
    mavenCentral()
}

/*
 * The deployment directory used to publish artifacts to Maven Central.
 */

val deployDirectory = "$rootDir/build/maven"

fun property(
    project: Project,
    name: String
): String {
    return project.extra[name] as String
}

fun propertyInt(
    project: Project,
    name: String
): Int {
    val text = property(project, name)
    return text.toInt()
}

/*
 * Configure Maven publishing. Artifacts are published to a local directory
 * so that they can be pushed to Maven Central in one step using brooklime.
 */

fun configurePublishingFor(project: Project) {
    val mavenCentralUsername =
        (project.findProperty("mavenCentralUsername") ?: "") as String
    val mavenCentralPassword =
        (project.findProperty("mavenCentralPassword") ?: "") as String

    val versionName = property(project, "VERSION_NAME")
    val packaging = project.extra["POM_PACKAGING"]

    project.publishing {
        publications {
            create<MavenPublication>("MavenPublication") {
                groupId = property(project, "GROUP")
                artifactId = property(project, "POM_ARTIFACT_ID")
                version = versionName

                pom {
                    name.set(property(project, "POM_NAME"))
                    description.set(property(project, "POM_DESCRIPTION"))
                    url.set(property(project, "POM_URL"))
                    scm {
                        connection.set(property(project, "POM_SCM_CONNECTION"))
                        developerConnection.set(property(project, "POM_SCM_DEV_CONNECTION"))
                        url.set(property(project, "POM_SCM_URL"))
                    }
                    licenses {
                        license {
                            name.set(property(project, "POM_LICENCE_NAME"))
                            url.set(property(project, "POM_LICENCE_URL"))
                        }
                    }
                }

                from(
                    when (packaging) {
                        "jar" -> {
                            project.components["java"]
                        }

                        "aar" -> {
                            project.components["release"]
                        }

                        "apk" -> {
                            project.components["release"]
                        }

                        else -> {
                            throw java.lang.IllegalArgumentException(
                                "Cannot set up publishing for packaging type $packaging"
                            )
                        }
                    }
                )
            }
        }

        repositories {
            maven {
                url = uri(deployDirectory)
            }
            maven {
                name = "SonatypeCentralSnapshots"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")

                credentials {
                    username = mavenCentralUsername
                    password = mavenCentralPassword
                }
            }
        }
    }
}

/*
 * A task that cleans up the Maven deployment directory. The "clean" tasks of
 * each project are configured to depend upon this tasks. This prevents any
 * deployment of stale artifacts to remote repositories.
 */

val cleanTask = task("cleanMavenDeployDirectory", Delete::class) {
    this.delete.add(deployDirectory)
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
            apply(plugin = "de.mannodermaus.android-junit5")

            /*
             * Configure the JVM toolchain version that we want to use for Kotlin.
             */

            val kotlin: KotlinAndroidProjectExtension =
                this.extensions["kotlin"] as KotlinAndroidProjectExtension

            kotlin.jvmToolchain(17)

            /*
             * Configure the various required Android properties.
             */

            val android: LibraryExtension =
                this.extensions["android"] as LibraryExtension

            android.namespace =
                property(this, "POM_ARTIFACT_ID")
            android.compileSdk =
                propertyInt(this, "ANDROID_SDK_COMPILE")

            android.defaultConfig {
                multiDexEnabled = true
                minSdk = propertyInt(this@allprojects, "ANDROID_SDK_COMPILE")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            android.compileOptions {
                encoding = "UTF-8"
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            android.testOptions {
                execution = "ANDROIDX_TEST_ORCHESTRATOR"
                animationsDisabled = true

                /*
                 * Enable the production of reports for all unit tests.
                 */

                unitTests {
                    isIncludeAndroidResources = true
                    all { test ->
                        test.reports.html.required = true
                        test.reports.junitXml.required = true
                    }
                }
            }
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

        "aar" -> {
            apply(plugin = "maven-publish")

            afterEvaluate {
                configurePublishingFor(this.project)
            }
        }

        "pom" -> {

        }
    }

    /*
     * Configure all "clean" tasks to depend upon the global Maven deployment directory cleaning
     * task.
     */

    tasks.matching { task -> task.name == "clean" }
        .forEach { task -> task.dependsOn(cleanTask) }
}
