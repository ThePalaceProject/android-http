pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("$rootDir/org.thepalaceproject.android.platform/build_libraries.toml"))
        }
    }

    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "org.librarysimplified.http"

include("org.librarysimplified.http.api")
include("org.librarysimplified.http.bearer_token")
include("org.librarysimplified.http.chucker")
include("org.librarysimplified.http.downloads")
include("org.librarysimplified.http.oauth_client_credentials")
include("org.librarysimplified.http.refresh_token")
include("org.librarysimplified.http.tests")
include("org.librarysimplified.http.uri_builder")
include("org.librarysimplified.http.vanilla")
