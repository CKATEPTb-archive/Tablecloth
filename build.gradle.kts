plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow").version("7.1.0")
    id("io.papermc.paperweight.userdev").version("1.3.3")
}

group = "ru.ckateptb"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven {
        url = uri("https://repo.minebench.de/")
        content {
            // this repository *only* contains artifacts with group "my.company"
            includeGroup("de.themoep")
        }
    }
}

dependencies {
    shadow("org.springframework:spring-context:5.3.9")
    shadow("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
    shadow("xyz.upperlevel.spigot.book:spigot-book-api:1.6")
    shadow("javax.annotation:javax.annotation-api:1.3.2")
    shadow("de.themoep:minedown:1.7.1-SNAPSHOT")
    shadow("com.zaxxer:HikariCP:3.4.2")
    shadow("com.j256.ormlite:ormlite-jdbc:6.0")
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")
    compileOnly("dev.jorel.CommandAPI:commandapi-core:6.4.0")
    compileOnly("org.projectlombok", "lombok", "1.18.22")
    annotationProcessor("org.projectlombok", "lombok", "1.18.22")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        dependencies {
            relocate("net.wesjd.anvilgui", "ru.ckateptb.tablecloth.gui.anvil")
            relocate("xyz.upperlevel.spigot.book", "ru.ckateptb.tablecloth.gui.book")
            relocate("de.themoep.minedown", "ru.ckateptb.tablecloth.minedown")
            relocate("com.j256.ormlite", "ru.ckateptb.tablecloth.storage.ormlite")
            relocate("com.zaxxer.hikari", "ru.ckateptb.tablecloth.storage.hikari")
        }
    }
    build {
        dependsOn(reobfJar)
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand("projectVersion" to project.version)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
