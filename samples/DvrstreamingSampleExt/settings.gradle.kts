import java.net.URI
import java.util.Properties
import java.io.FileInputStream

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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {

            url = URI("https://raw.githubusercontent.com/adswizz/ad-sdk-android/master/releases")

            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                val envToken = System.getenv("ADSWIZZ_TOKEN")
                if(envToken != null){
                    value = envToken
                }
                else {
                    val properties = Properties()
                    properties. load(FileInputStream(File(rootProject.projectDir.path, "local.properties")))
                    value = properties["adswizzMavenToken"].toString()
                }
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}

rootProject.name = "DvrstreamingSampleExt"
include(":app")
 