plugins {
	java
}

repositories {
	mavenCentral()
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
	implementation(files("dependencies/jsqlparser.jar"))
	testImplementation("junit:junit:4.12")
	testImplementation(platform("org.junit:junit-bom:5.9.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
}

apply(plugin="eclipse")

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
