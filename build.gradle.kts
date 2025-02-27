plugins {
	java
	id("com.gradleup.shadow") version "8.3.5"
}

group = "de.neo.jagil"
description = "JAGIL-Loader"
version = "4.0-beta.38"

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
	mavenCentral()
	mavenLocal()
	maven {
		name = "keksServerRepositorySnapshots"
		url = uri("https://repo.keks-server.de/snapshots")
	}
	maven {
		name = "papermc"
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
}

dependencies {
	compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
	implementation("de.neo8.jagil:JAGIL:$version")
}

tasks.processResources {
	files("plugin.yml") {
		expand("pluginVersion" to version)
	}
}