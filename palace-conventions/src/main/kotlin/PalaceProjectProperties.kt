data class PalaceProjectProperties(
  val adobeDrmEnabled: Boolean,
  val androidSdkCompile: Int,
  val androidSdkMinimum: Int,
  val androidSdkTarget: Int,
  val checkSemanticVersioning: Boolean,
  val enableKtLint: Boolean,
  val enableSigning: Boolean,
  val group: String,
  val jdkBuild: Int,
  val jdkBytecodeTarget: Int,
  val pomArtifactId: String,
  val pomAutomaticModuleName: String,
  val pomDescription: String,
  val pomInceptionYear: Int,
  val pomLicenceDist: String,
  val pomLicenceName: String,
  val pomLicenceUrl: String,
  val pomName: String,
  val pomScmConnection: String,
  val pomScmDevConnection: String,
  val pomScmUrl: String,
  val pomUrl: String,
  val publishSources: Boolean,
  val versionName: String,
  val versionPrevious: String,
) {
  companion object {
    fun fromMap(map: Map<String, Any?>): PalaceProjectProperties {
      fun string(key: String): String =
        map[key]?.toString()
          ?: error("Missing required property: $key")

      fun int(key: String): Int = string(key).toInt()

      fun bool(key: String): Boolean = string(key).toBooleanStrict()

      return PalaceProjectProperties(
        adobeDrmEnabled = bool("org.thepalaceproject.adobeDRM.enabled"),
        androidSdkCompile = int("org.thepalaceproject.build.androidSDKCompile"),
        androidSdkMinimum = int("org.thepalaceproject.build.androidSDKMinimum"),
        androidSdkTarget = int("org.thepalaceproject.build.androidSDKTarget"),
        checkSemanticVersioning = bool("org.thepalaceproject.build.checkSemanticVersioning"),
        enableKtLint = bool("org.thepalaceproject.build.enableKtLint"),
        enableSigning = bool("org.thepalaceproject.build.enableSigning"),
        group = string("GROUP"),
        jdkBuild = int("org.thepalaceproject.build.jdkBuild"),
        jdkBytecodeTarget = int("org.thepalaceproject.build.jdkBytecodeTarget"),
        pomArtifactId = string("POM_ARTIFACT_ID"),
        pomAutomaticModuleName = string("POM_AUTOMATIC_MODULE_NAME"),
        pomDescription = string("POM_DESCRIPTION"),
        pomInceptionYear = int("POM_INCEPTION_YEAR"),
        pomLicenceDist = string("POM_LICENCE_DIST"),
        pomLicenceName = string("POM_LICENCE_NAME"),
        pomLicenceUrl = string("POM_LICENCE_URL"),
        pomName = string("POM_NAME"),
        pomScmConnection = string("POM_SCM_CONNECTION"),
        pomScmDevConnection = string("POM_SCM_DEV_CONNECTION"),
        pomScmUrl = string("POM_SCM_URL"),
        pomUrl = string("POM_URL"),
        publishSources = bool("org.thepalaceproject.build.publishSources"),
        versionName = string("VERSION_NAME"),
        versionPrevious = string("VERSION_PREVIOUS"),
      )
    }
  }
}
