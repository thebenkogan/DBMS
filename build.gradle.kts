plugins {
	application
	eclipse
	alias(libs.plugins.spotless)
}

repositories {
	mavenCentral()
}

spotless {
	java {
	    importOrder()
	    removeUnusedImports()
	    palantirJavaFormat()
	    formatAnnotations()
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

application {
	mainClass.set("com.dbms.Interpreter")
}

dependencies {
	implementation(libs.jsqlparser)
	implementation(libs.guava)
	testImplementation(libs.junit.jupiter)
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
