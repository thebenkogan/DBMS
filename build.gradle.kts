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

task("interpreter", JavaExec::class) {
	mainClass.set("com.dbms.Interpreter")
	classpath = sourceSets["main"].runtimeClasspath
}

task("benchmarking", JavaExec::class) {
	mainClass.set("com.dbms.analytics.Benchmarking")
	classpath = sourceSets["main"].runtimeClasspath
}
