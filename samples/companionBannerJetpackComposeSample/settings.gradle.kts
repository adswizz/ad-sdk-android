import java.io.FileInputStream
import java.net.URI
import java.util.Properties

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
val properties = Properties().apply {
    load(FileInputStream(File(rootProject.projectDir.path, "local.properties")))
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {

            url = URI("https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases")

            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                val token = properties["adswizzMavenToken"].toString()
                value = token
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

rootProject.name = "JetpackComposeSample"
include(":app")
 