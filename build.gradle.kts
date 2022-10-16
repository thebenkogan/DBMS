plugins {
	java
	application
	eclipse
	id("com.diffplug.spotless") version "6.11.0"
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
	implementation("com.github.jsqlparser:jsqlparser:4.5")
	implementation("com.google.guava:guava:31.1-jre")
	testImplementation("junit:junit:4.12")
	testImplementation(platform("org.junit:junit-bom:5.9.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
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
