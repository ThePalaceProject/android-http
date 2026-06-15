import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
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
}
