apply {
    from("config.gradle.kts")
}

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

val ideVersion: String by extra
//val baseVersion: String by extra
val javaVersion: String by extra
val since: String by extra
val until: String? by extra
val mPluginName: String by extra
val mPluginId: String by extra
val mPluginVersion: String by extra


repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/google")
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2")
    type.set("IC") // Target IDE Platform

    //依赖IDEA平台的java插件
    plugins.set(listOf("com.intellij.java"))
}

java {
    sourceSets.main {
        this.java.srcDir("src/main/java")
    }
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }

    compileKotlin {
        kotlinOptions{
            jvmTarget = javaVersion
        }
    }

    patchPluginXml{
        pluginId.set(mPluginId)
        sinceBuild.set(since)
        if(until.isNullOrEmpty()){
            untilBuild.set(provider {  null })
        } else {
            untilBuild.set(until)
        }
        version.set(mPluginVersion)
        changeNotes.set("""
          <ul>
            <li>v1.0 Demo Init.</li>
          </ul>
          """)
    }

    jar {
        archiveFileName.set("wildfire-demo.jar")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
