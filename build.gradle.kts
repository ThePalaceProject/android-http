import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Verify
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

    /*
     * Download plugin. Used to fetch artifacts such as Scando during the build.
     *
     * https://plugins.gradle.org/plugin/de.undercouch.download
     */

    id("de.undercouch.download")
        .version("5.4.0")
        .apply(false)

    id("maven-publish")
}

repositories {
    mavenCentral()
}

/*
 * The various paths used during the build.
 */

val palaceRootBuildDirectory =
    "$rootDir/build"
val palaceDeployDirectory =
    "$palaceRootBuildDirectory/maven"
val palaceScandoJarFile =
    "$palaceRootBuildDirectory/scando.jar"

/**
 * Convenience functions to read strongly-typed values from property files.
 */

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

fun propertyBoolean(
    project: Project,
    name: String
): Boolean {
    val text = property(project, name)
    return text.toBooleanStrict()
}

/**
 * Configure Maven publishing. Artifacts are published to a local directory
 * so that they can be pushed to Maven Central in one step using brooklime.
 */

fun configurePublishingFor(project: Project) {
    val mavenCentralUsername =
        (project.findProperty("mavenCentralUsername") ?: "") as String
    val mavenCentralPassword =
        (project.findProperty("mavenCentralPassword") ?: "") as String

    val versionName =
        property(project, "VERSION_NAME")
    val packaging =
        property(project, "POM_PACKAGING")

    val publishSources =
        propertyBoolean(project, "org.thepalaceproject.publishSources")

    /*
     * Create an empty JavaDoc jar. Required for Maven Central deployments.
     */

    val taskJavadocEmpty =
        project.task("JavadocEmptyJar", org.gradle.jvm.tasks.Jar::class) {
            this.archiveClassifier = "javadoc"
        }

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

                artifact(taskJavadocEmpty)

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
                name = "Directory"
                url = uri(palaceDeployDirectory)
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

    /*
     * If source publications are disabled in the project properties, it seems that the only
     * way to stop the Android plugins from publishing sources is to manually "disable" the
     * publication tasks by deleting all of the actions within the tasks, and then specifying
     * a dependency on our own task that produces an empty jar file.
     */

    if (!publishSources) {
        logger.info("org.thepalaceproject.publishSources is false, so source jars are disabled.")

        val taskSourcesEmpty =
            project.task("SourcesEmptyJar", org.gradle.jvm.tasks.Jar::class) {
                this.archiveClassifier = "sources"
            }

        project.tasks.matching { task -> task.name.endsWith("SourcesJar") }
            .forEach { task ->
                task.actions.clear()
                task.dependsOn.add(taskSourcesEmpty)
            }
    }
}

/*
 * A task that cleans up the Maven deployment directory. The "clean" tasks of
 * each project are configured to depend upon this task. This prevents any
 * deployment of stale artifacts to remote repositories.
 */

val cleanTask = task("cleanMavenDeployDirectory", Delete::class) {
    this.delete.add(palaceDeployDirectory)
}

/**
 * A function to download and verify the Scando jar file.
 *
 * @return The verification task
 */

fun createScandoDownloadTask(project: Project): Task {
    val scandoVersion =
        "1.0.0"
    val scandoSHA256 =
        "08fba5fc4bc3b5a49d205a4c38356dc8c7e01f4963adb661b67f9d2ed23751ae"
    val scandoSource =
        "https://repo1.maven.org/maven2/com/io7m/scando/com.io7m.scando.cmdline/${scandoVersion}/com.io7m.scando.cmdline-${scandoVersion}-main.jar"

    val scandoMakeDirectory =
        project.task("ScandoMakeDirectory") {
            mkdir(palaceRootBuildDirectory)
        }

    val scandoDownload =
        project.task("ScandoDownload", Download::class) {
            src(scandoSource)
            dest(file(palaceScandoJarFile))
            overwrite(true)
            this.dependsOn.add(scandoMakeDirectory)
        }

    return project.task("ScandoDownloadVerify", Verify::class) {
        src(file(palaceScandoJarFile))
        checksum(scandoSHA256)
        algorithm("SHA-256")
        this.dependsOn(scandoDownload)
    }
}

/**
 * A task to execute Scando to analyze semantic versioning.
 */

fun createScandoAnalyzeTask(project: Project): Task {
    val group =
        property(project, "GROUP")
    val artifactId =
        property(project, "POM_ARTIFACT_ID")
    val versionCurrent =
        property(project, "VERSION_NAME")
    val versionPrevious =
        property(project, "VERSION_PREVIOUS")

    val oldGroup =
        group.replace('.', '/')
    val oldPath =
        "${oldGroup}/${artifactId}/${versionPrevious}/${artifactId}-${versionPrevious}.aar"
    val oldUrl =
        "https://repo1.maven.org/maven2/${oldPath}"

    val commandLineArguments: List<String> = arrayListOf(
        "java",
        "-jar",
        palaceScandoJarFile,
        "--excludeList",
        "${project.rootDir}/VERSIONING.txt",
        "--oldJarUri",
        oldUrl,
        "--oldJarVersion",
        versionPrevious,
        "--ignoreMissingOld",
        "--newJar",
        "${project.buildDir}/outputs/aar/${artifactId}-debug.aar",
        "--newJarVersion",
        versionCurrent,
        "--textReport",
        "${project.buildDir}/scando-report.txt",
        "--htmlReport",
        "${project.buildDir}/scando-report.html"
    )

    return project.task("ScandoAnalyze", Exec::class) {
        commandLine = commandLineArguments
    }
}

/*
 * Create a task in the root project that downloads Scando.
 */

lateinit var scandoDownloadTask : Task

rootProject.afterEvaluate {
    apply(plugin = "de.undercouch.download")
    scandoDownloadTask = createScandoDownloadTask(this)
}

allprojects {

    /*
     * Configure the project metadata.
     */

    this.group = property(this, "GROUP")
    this.version = property(this, "VERSION_NAME")

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
                propertyInt(this, "org.thepalaceproject.androidSDKCompile")

            android.defaultConfig {
                multiDexEnabled = true
                minSdk = propertyInt(this@allprojects, "org.thepalaceproject.androidSDKMinimum")
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

            /*
             * Configure semantic versioning analysis.
             */

            afterEvaluate {
                val enableSemanticVersionChecks =
                    propertyBoolean(this, "org.thepalaceproject.checkSemanticVersioning")

                if (enableSemanticVersionChecks) {
                    val verifyActual = createScandoAnalyzeTask(this)
                    verifyActual.dependsOn.add(scandoDownloadTask)
                    verifyActual.dependsOn.add("assembleDebug")

                    val verifyTask = project.task("verifySemanticVersioning")
                    verifyTask.dependsOn.add(verifyActual)
                } else {
                    // Create a do-nothing task to keep interface compatibility.
                    project.task("verifySemanticVersioning")
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

                this.reports.html.required = true
                this.reports.junitXml.required = true
            }
        }
    }

    /*
     * Configure publishing.
     */

    when (extra["POM_PACKAGING"]) {
        "jar", "aar" -> {
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