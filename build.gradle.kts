plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = "net.bitpot"
version = providers.gradleProperty("pluginVersion").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        rubymine(providers.gradleProperty("platformVersion"))
        bundledPlugin("org.jetbrains.plugins.ruby")
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.13"
    }
}
