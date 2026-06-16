import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.core.extra

class PalaceKtLint : Plugin<Project> {
  override fun apply(project: Project) {
    val properties = PalaceProjectProperties.fromMap(project.extra.properties)

    val rootDir = project.rootDir
    val ktlintVersion = "1.8.0"
    val ktlintJar = "$rootDir/ktlint.jar"

    val patterns =
      listOf(
        "*/src/**/*.kt",
        "*/build.gradle.kts",
        "build.gradle.kts",
        "!*/src/test/**",
      )

    val download: TaskProvider<Exec> =
      project.tasks.register("ktlintDownload", Exec::class.java) {
        this.group = "verification"
        this.commandLine(
          "java",
          "org.thepalaceproject.android.platform/DownloadVerify.java",
          "https://repo1.maven.org/maven2/com/pinterest/ktlint/ktlint-cli/$ktlintVersion/ktlint-cli-$ktlintVersion-all.jar",
          ktlintJar,
          "369ad2b789f95a011f807e1fcb690ccef80bd7cd014fd139e73ae82dcc0baeab",
        )
      }

    val check: TaskProvider<Exec> =
      project.tasks.register("ktlintCheck", Exec::class.java) {
        this.group = "verification"
        this.dependsOn(download)
        this.commandLine(listOf("java", "-jar", ktlintJar) + patterns)
      }

    val format: TaskProvider<Exec> =
      project.tasks.register("ktlintFormat", Exec::class.java) {
        this.group = "formatting"
        this.dependsOn(download)
        this.commandLine(listOf("java", "-jar", ktlintJar, "-F") + patterns)
      }

    if (properties.enableKtLint) {
      project.tasks.named("clean") {
        dependsOn(check)
      }
    }
  }
}
