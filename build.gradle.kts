val gradleVersionRequired = "9.4.1"
val gradleVersionReceived = gradle.gradleVersion

if (gradleVersionRequired != gradleVersionReceived) {
    throw GradleException(
        "Gradle version $gradleVersionRequired is required to run this build. You are using Gradle $gradleVersionReceived",
    )
}

plugins {
    signing

    /*
     * Android Junit5 plugin. Required to run JUnit 5 tests on Android projects.
     *
     * https://github.com/mannodermaus/android-junit5
     */

    id("de.mannodermaus.android-junit")
        .version("2.0.1")
        .apply(false)

    /*
     * https://developers.google.com/android/guides/google-services-plugin
     */

    id("com.google.gms.google-services")
        .version("4.3.15")
        .apply(false)

    /*
     * https://firebase.google.com/docs/crashlytics/get-started?platform=android
     */

    id("com.google.firebase.crashlytics")
        .version("3.0.2")
        .apply(false)

    id("maven-publish")
}
