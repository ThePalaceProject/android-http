import PalaceConfiguration.PackagingType.AAR
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
      PalaceConfiguration.configureKotlin(this, properties)
    }
    project.extensions.configure(JavaPluginExtension::class.java) {
      PalaceConfiguration.configureJava(this, properties)
    }
    project.extensions.configure(LibraryExtension::class.java) {
      PalaceConfiguration.configureAndroidLibrary(this, properties)
    }
    PalaceConfiguration.configureDisableTransitiveDependencies(project)
    PalaceConfiguration.configureDisableTests(project)
    PalaceConfiguration.configurePublishing(properties, project, AAR)
  }
}
