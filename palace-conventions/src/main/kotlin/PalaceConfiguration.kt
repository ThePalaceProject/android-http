import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

object PalaceConfiguration {
  enum class PackagingType {
    JAR,
    AAR,
    APK,
    POM,
  }

  /**
   * Configure the Java compiler to produce the expected bytecode versions, and to require
   * a particular build JDK version.
   */

  fun configureJava(
    ex: JavaPluginExtension,
    properties: PalaceProjectProperties,
  ) {
    ex.sourceCompatibility =
      JavaVersion.toVersion(properties.jdkBytecodeTarget)
    ex.targetCompatibility =
      JavaVersion.toVersion(properties.jdkBytecodeTarget)

    ex.toolchain.languageVersion.set(
      JavaLanguageVersion.of(properties.jdkBuild),
    )
  }

  /**
   * Configure Kotlin to use the build JDK toolchain.
   */

  fun configureKotlin(
    ex: KotlinAndroidExtension,
    properties: PalaceProjectProperties,
  ) {
    ex.jvmToolchain(properties.jdkBuild)
  }

  /**
   * Configure the properties necessary to produce an Android AAR.
   */

  fun configureAndroidLibrary(
    ex: LibraryExtension,
    properties: PalaceProjectProperties,
  ) {
    ex.namespace =
      properties.pomArtifactId
    ex.compileSdk =
      properties.androidSdkCompile
    ex.defaultConfig.minSdk =
      properties.androidSdkMinimum

    ex.compileOptions {
      this.encoding = "UTF-8"
      this.isCoreLibraryDesugaringEnabled = true
      this.sourceCompatibility = JavaVersion.toVersion(properties.jdkBytecodeTarget)
      this.targetCompatibility = JavaVersion.toVersion(properties.jdkBytecodeTarget)
    }

    ex.testOptions {
      this.execution = "ANDROIDX_TEST_ORCHESTRATOR"
      this.animationsDisabled = true

      /*
       * Enable the production of reports for all unit tests.
       */

      this.unitTests {
        this.isIncludeAndroidResources = true

        this.all { test ->
          // Required for the Mockito ByteBuddy agent on modern VMs.
          test.systemProperty("jdk.attach.allowAttachSelf", "true")
          test.reports.html.required
            .set(true)
          test.reports.junitXml.required
            .set(true)
          test.useJUnitPlatform()
        }
      }
    }
  }

  /**
   * Disable transitive dependency handling. Android dependency trees are too large and too
   * riddled with conflicts for transitive dependency handling to work. Instead, modules must
   * explicitly depend on dependencies and application bundles are forced to list every single
   * module upon which they depend.
   */

  fun configureDisableTransitiveDependencies(project: Project) {
    /*
     * The dependency configurations that are allowed to be transitive. Most of these are present
     * just because the various Android build system garbage will break catastrophically if
     * transitive dependencies aren't allowed.
     */

    val transitiveConfigurations =
      setOf(
        "androidTestDebugImplementation",
        "androidTestDebugImplementationDependenciesMetadata",
        "androidTestImplementation",
        "androidTestImplementationDependenciesMetadata",
        "androidTestReleaseImplementation",
        "androidTestReleaseImplementationDependenciesMetadata",
        "annotationProcessor",
        "coreLibraryDesugaring",
        "debugAndroidTestCompilationImplementation",
        "debugAndroidTestImplementation",
        "debugAndroidTestImplementationDependenciesMetadata",
        "debugAnnotationProcessor",
        "debugAnnotationProcessorClasspath",
        "debugUnitTestCompilationImplementation",
        "debugUnitTestImplementation",
        "debugUnitTestImplementationDependenciesMetadata",
        "kotlinBuildToolsApiClasspath",
        "kotlinCompilerClasspath",
        "kotlinCompilerPluginClasspath",
        "kotlinCompilerPluginClasspathDebug",
        "kotlinCompilerPluginClasspathDebugAndroidTest",
        "kotlinCompilerPluginClasspathDebugUnitTest",
        "kotlinCompilerPluginClasspathMain",
        "kotlinCompilerPluginClasspathRelease",
        "kotlinCompilerPluginClasspathReleaseUnitTest",
        "kotlinCompilerPluginClasspathTest",
        "kotlinKlibCommonizerClasspath",
        "kotlinNativeCompilerPluginClasspath",
        "kotlinScriptDef",
        "kotlinScriptDefExtensions",
        "mainSourceElements",
        "releaseAnnotationProcessor",
        "releaseAnnotationProcessorClasspath",
        "releaseUnitTestCompilationImplementation",
        "releaseUnitTestImplementation",
        "releaseUnitTestImplementationDependenciesMetadata",
        "testDebugImplementation",
        "testDebugImplementationDependenciesMetadata",
        "testFixturesDebugImplementation",
        "testFixturesDebugImplementationDependenciesMetadata",
        "testFixturesImplementation",
        "testFixturesImplementationDependenciesMetadata",
        "testFixturesReleaseImplementation",
        "testFixturesReleaseImplementationDependenciesMetadata",
        "testImplementation",
        "testImplementationDependenciesMetadata",
        "testReleaseImplementation",
        "testReleaseImplementationDependenciesMetadata",
      )

    project.afterEvaluate {
      this.configurations.forEach { cfg ->
        cfg.isTransitive = transitiveConfigurations.contains(cfg.name)
      }
    }
  }

  /*
   * Configure all "test" tasks to be disabled. The tests are enabled only in those modules
   * that specifically ask for them. Why do this? Because the Android plugins do lots of
   * expensive per-module configuration for tests that don't exist.
   */

  fun configureDisableTests(project: Project) {
    project.afterEvaluate {
      this.tasks
        .matching { task -> task.name.contains("Test") }
        .forEach { task -> task.enabled = false }
    }
  }

  fun configurePublishing(
    properties: PalaceProjectProperties,
    project: Project,
    packagingType: PackagingType,
  ) {
    val palaceDeployDirectory =
      "${project.rootDir}/maven"
    val mavenCentralUsername =
      (project.findProperty("mavenCentralUsername") ?: "") as String
    val mavenCentralPassword =
      (project.findProperty("mavenCentralPassword") ?: "") as String

    project.pluginManager.apply(MavenPublishPlugin::class.java)

    /*
     * Create an empty JavaDoc jar. Required for Maven Central deployments.
     */

    val taskJavadocEmpty =
      project.tasks.register("JavadocEmptyJar", Jar::class.java) {
        this.archiveClassifier.set("javadoc")
      }

    /*
     * Create a publication. Note that the name of the publication must be unique across all
     * modules, because the broken Gradle signing plugin will create a signing task for each
     * one that, in the case of a name conflict, will silently overwrite the previous signing
     * task.
     */

    val publishing =
      project.extensions.getByType(PublishingExtension::class.java)

    val publication =
      publishing.publications.create(
        "_${project.name}_MavenPublication",
        MavenPublication::class.java,
      ) {
        this.groupId = properties.group
        this.artifactId = properties.pomArtifactId
        this.version = properties.versionName

      /*
       * https://central.sonatype.org/publish/requirements/#sufficient-metadata
       */

        this.pom {
          this.name.set(properties.pomName)
          this.description.set(properties.pomDescription)
          this.url.set(properties.pomUrl)

          this.scm {
            this.connection.set(properties.pomScmConnection)
            this.developerConnection.set(properties.pomScmDevConnection)
            this.url.set(properties.pomScmUrl)
          }

          this.licenses {
            this.license {
              this.name.set(properties.pomLicenceName)
              this.url.set(properties.pomLicenceUrl)
            }
          }

          this.developers {
            this.developer {
              this.name.set("The Palace Project")
              this.email.set("info@thepalaceproject.org")
              this.organization.set("The Palace Project")
              this.organizationUrl.set("https://thepalaceproject.org/")
            }
          }
        }

        this.artifact(taskJavadocEmpty)
      }

    when (packagingType) {
      PackagingType.JAR -> {
        TODO()
      }

      /*
       * The AGP plugin is still so broken after all of these years that the only way to produce
       * a publication is to wait until the project has been evaluated.
       */

      PackagingType.AAR -> {
        project.extensions.configure(LibraryExtension::class.java) {
          this.publishing {
            this.singleVariant("release") {
              if (properties.publishSources) {
                this.withSourcesJar()
              }
            }
          }
        }

        project.afterEvaluate {
          publication.from(components.getByName("release"))
        }
      }

      PackagingType.APK -> {
        TODO()
      }

      PackagingType.POM -> {
        TODO()
      }
    }

    publishing.repositories.apply {
      maven {
        this.name = "Directory"
        this.url = project.uri(palaceDeployDirectory)
      }

      if (properties.versionName.endsWith("-SNAPSHOT")) {
        maven {
          this.name = "SonatypeCentralSnapshots"
          this.url =
            project.uri(
              "https://central.sonatype.com/repository/maven-snapshots/",
            )

          this.credentials {
            this.username = mavenCentralUsername
            this.password = mavenCentralPassword
          }
        }
      }
    }

    if (!properties.publishSources) {
      val sourcesJar =
        project.tasks.register("sourcesJar", Jar::class.java) {
          this.archiveClassifier.set("sources")
        }

      project.tasks
        .matching { task ->
          task.name.endsWith("SourcesJar")
        }.configureEach {
          this.actions.clear()
          this.dependsOn(sourcesJar)
        }
    }

    if (properties.enableSigning) {
      project.pluginManager.apply(SigningPlugin::class.java)
      val signing = project.extensions.getByType(SigningExtension::class.java)

      signing.useGpgCmd()
      signing.sign(publishing.publications)
    }
  }
}
