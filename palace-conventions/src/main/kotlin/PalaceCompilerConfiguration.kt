import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

object PalaceCompilerConfiguration {

  fun configureJava(
    ex: JavaPluginExtension,
    properties: PalaceProjectProperties,
  ) {
    ex.sourceCompatibility =
      JavaVersion.toVersion(properties.jdkBytecodeTarget)
    ex.targetCompatibility =
      JavaVersion.toVersion(properties.jdkBytecodeTarget)

    ex.toolchain.languageVersion.set(
      JavaLanguageVersion.of(properties.jdkBuild)
    )
  }

  fun configureKotlin(
    ex: KotlinAndroidExtension,
    properties: PalaceProjectProperties
  ) {
    ex.jvmToolchain(properties.jdkBuild)
  }

  fun configureAndroidLibrary(
    ex: LibraryExtension,
    properties: PalaceProjectProperties
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

        all { test ->
          // Required for the Mockito ByteBuddy agent on modern VMs.
          test.systemProperty("jdk.attach.allowAttachSelf", "true")
          test.reports.html.required.set(true)
          test.reports.junitXml.required.set(true)
        }
      }
    }
  }

  fun configureDisableTransitiveDependencies(
    project: Project
  ) {
    /*
     * The dependency configurations that are allowed to be transitive. Most of these are present
     * just because the various Android build system garbage will break catastrophically if
     * transitive dependencies aren't allowed.
     */

    val transitiveConfigurations = setOf(
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

    val configurationsActual = mutableSetOf<String>()
    project.afterEvaluate {
      configurations.forEach { cfg ->
        configurationsActual.add(cfg.name)
        cfg.isTransitive = transitiveConfigurations.contains(cfg.name)
      }
    }
  }

  fun configureDisableTests(
    project: Project
  ) {
    /*
     * Configure all "test" tasks to be disabled. The tests are enabled only in those modules
     * that specifically ask for them. Why do this? Because the Android plugins do lots of
     * expensive per-module configuration for tests that don't exist.
     */

    project.afterEvaluate {
      tasks.matching { task -> task.name.contains("Test") }
        .forEach { task -> task.enabled = false }
    }
  }
}
