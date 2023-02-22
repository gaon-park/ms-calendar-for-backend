import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.2-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.0"
	id("io.gitlab.arturbosch.detekt") version "1.21.0"
	kotlin("jvm") version "1.7.22"
	kotlin("plugin.spring") version "1.7.22"
	kotlin("plugin.jpa") version "1.7.22"
}

group = "com.maple"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-quartz:3.0.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation("io.jsonwebtoken:jjwt-api:0.11.2")
	implementation("org.json:json:20220924")
	implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.4")
	testImplementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.0.2")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.0.2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
	implementation("org.jsoup:jsoup:1.15.3")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter:1.2.8.RELEASE")
	implementation("org.springframework.cloud:spring-cloud-gcp-storage:1.2.8.RELEASE")

	// https://mvnrepository.com/artifact/com.auth0/java-jwt
	implementation("com.auth0:java-jwt:4.3.0")

	compileOnly("org.projectlombok:lombok")

	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("mysql:mysql-connector-java")

	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
	testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.4")
	testImplementation("io.mockk:mockk:1.13.3")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
