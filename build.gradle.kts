plugins {
    java
    signing
    `maven-publish`
    id("com.github.johnrengelman.shadow").version("7.1.0")
    id("io.papermc.paperweight.userdev").version("1.3.3")
}

group = "ru.ckateptb"
version = "1.0.2-SNAPSHOT"
var githubName = "Tablecloth"
var githubOwner = "CKATEPTb"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    if (!isSnapshot()) {
        withJavadocJar()
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
            includeGroup("de.themoep")
        }
    }
}

dependencies {
    implementation("org.springframework:spring-context:5.3.9")
    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
    implementation("xyz.upperlevel.spigot.book:spigot-book-api:1.6")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("de.themoep:minedown:1.7.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("com.j256.ormlite:ormlite-jdbc:6.0")
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")
    compileOnly("dev.jorel.CommandAPI:commandapi-core:6.4.0")
    compileOnly("org.projectlombok", "lombok", "1.18.22")
    annotationProcessor("org.projectlombok", "lombok", "1.18.22")
}

tasks {
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.${archiveExtension.getOrElse("jar")}")
        dependencies {
            relocate("net.wesjd.anvilgui", "ru.ckateptb.tablecloth.gui.anvil")
            relocate("xyz.upperlevel.spigot.book", "ru.ckateptb.tablecloth.gui.book")
            relocate("de.themoep.minedown", "ru.ckateptb.tablecloth.minedown")
            relocate("com.j256.ormlite", "ru.ckateptb.tablecloth.storage.ormlite")
            relocate("com.zaxxer.hikari", "ru.ckateptb.tablecloth.storage.hikari")
        }
    }
    build {
        dependsOn(reobfJar, shadowJar)
    }
    withType<Sign>().configureEach {
        onlyIf { !isSnapshot() }
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    named<Copy>("processResources") {
        filesMatching("plugin.yml") {
            expand("projectVersion" to project.version)
        }
        from("LICENSE") {
            rename { "${project.name.toUpperCase()}_${it}" }
        }
    }
}

publishing {
    publications {
        publications.create<MavenPublication>("maven") {
            artifacts {
                artifact(tasks.shadowJar) {
                    classifier = ""
                }
                if (!isSnapshot()) {
                    artifact(tasks.javadoc)
                }
                artifact(tasks["sourcesJar"])
            }
            pom {
                name.set(project.name)
                url.set("https://github.com/${githubOwner}/${githubName}")
                licenses {
                    license {
                        name.set("The GNU Affero General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/${githubOwner}/${githubName}.git")
                    developerConnection.set("scm:git:ssh://git@github.com/${githubOwner}/${githubName}.git")
                    url.set("https://github.com/${githubOwner}/${githubName}")
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/${githubOwner}/${githubName}/issues")
                }
            }
        }
        repositories {
            maven {
                name = githubName
                url = uri("https://maven.pkg.github.com/${githubOwner}/${githubName}")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}


signing {
    sign(publishing.publications["maven"])
}

fun isSnapshot() = project.version.toString().endsWith("-SNAPSHOT")
