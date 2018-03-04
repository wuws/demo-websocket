import com.github.spotbugs.SpotBugsTask
import groovy.lang.Closure
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("com.palantir.git-version").version("0.10.1")
    kotlin("jvm").version("1.2.30")
    id("org.springframework.boot").version("2.0.0.RELEASE").apply(false)
    id("io.spring.dependency-management").version("1.0.4.RELEASE")
    id("com.github.spotbugs").version("1.6.0")
    id("com.gorylenko.gradle-git-properties").version("1.4.2")
    id("com.github.ben-manes.versions").version("0.17.0")
}

val kotlinVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "org.jetbrains.kotlin.jvm.gradle.plugin" }?.moduleVersion
}
project.ext["kotlin.version"] = kotlinVersion
val springBootVersion: String? by extra {
    buildscript.configurations["classpath"]
            .resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "org.springframework.boot.gradle.plugin" }?.moduleVersion
}
val projectGitVersion: String = (project.ext["gitVersion"] as Closure<*>)() as String

allprojects {
    group = "net.devopssolutions"
    version = projectGitVersion

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.kotlin.jvm")
        plugin("pmd")
        plugin("findbugs")
        plugin("com.github.ben-manes.versions")
        plugin("io.spring.dependency-management")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
        dependencies {
            dependency("org.springframework.boot:spring-boot-starter-web:$springBootVersion") {
                exclude("org.springframework.boot:spring-boot-starter-tomcat")
            }
        }
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        compile("org.jetbrains.kotlin:kotlin-reflect")

        testCompile("junit:junit")
    }

    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

project(":ws-client") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        compile(project(":ws-models"))

        compile("org.springframework.boot:spring-boot-starter-web") {
            exclude("org.springframework.boot:spring-boot-starter-tomcat")
        }
        compile("org.springframework.boot:spring-boot-starter-undertow")
        compile("org.springframework.boot:spring-boot-starter-websocket")
        compile("org.springframework.boot:spring-boot-actuator")
//        compile("org.springframework.boot:spring-boot-devtools")
        compile("org.springframework.boot:spring-boot-configuration-processor")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin")
    }

    tasks {
        "bootRun"(type = JavaExec::class) {
            systemProperty("spring.output.ansi.enabled", "always")
        }
    }
}

project(":ws-server") {
    apply {
        plugin("org.springframework.boot")
    }

    dependencies {
        compile(project(":ws-models"))

        compile("org.springframework.boot:spring-boot-starter-web") {
            exclude("org.springframework.boot:spring-boot-starter-tomcat")
        }
        compile("org.springframework.boot:spring-boot-starter-undertow")
        compile("org.springframework.boot:spring-boot-starter-websocket")
        compile("org.springframework.boot:spring-boot-actuator")
//        compile("org.springframework.boot:spring-boot-devtools")
        compile("org.springframework.boot:spring-boot-configuration-processor")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin")

//        compile group: "org.xerial.snappy", name: "snappy-java", version: "1.1.7.1"
        compile("net.jpountz.lz4:lz4:1.3")
        compile("de.ruedigermoeller:fst:2.57")
    }


    tasks {
        "bootRun"(type = JavaExec::class) {
            systemProperty("spring.output.ansi.enabled", "always")
        }
    }
}

tasks {
    "wrapper"(type = Wrapper::class) {
        gradleVersion = "4.6"
        distributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL
    }
}