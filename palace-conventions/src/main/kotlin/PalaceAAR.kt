import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.internal.extensions.core.extra
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension

class PalaceAAR : Plugin<Project> {
  override fun apply(project: Project) {
    val properties = PalaceProjectProperties.fromMap(project.extra.properties)

    project.pluginManager.apply("com.android.library")
    project.extensions.configure(KotlinAndroidExtension::class.java) {
      PalaceCompilerConfiguration.configureKotlin(this, properties)
    }
    project.extensions.configure(JavaPluginExtension::class.java) {
      PalaceCompilerConfiguration.configureJava(this, properties)
    }
    project.extensions.configure(LibraryExtension::class.java) {
      PalaceCompilerConfiguration.configureAndroidLibrary(this, properties)
    }
    PalaceCompilerConfiguration.configureDisableTransitiveDependencies(project)
    PalaceCompilerConfiguration.configureDisableTests(project)
  }
}
