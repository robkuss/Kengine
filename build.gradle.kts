plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.3.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // LWJGL dependencies
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-freetype:$lwjglVersion")
    
    // Natives
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-freetype:$lwjglVersion:natives-windows")
}

// Create a new configuration for natives
configurations {
    create("natives")
}

// Add native dependencies to the custom configuration
dependencies {
    "natives"("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
    "natives"("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
    "natives"("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows")
    "natives"("org.lwjgl:lwjgl-freetype:$lwjglVersion:natives-windows")
}

// Copy natives task
tasks.register<Copy>("copyNatives") {
    from(configurations["natives"].map { zipTree(it) }) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Adjust as necessary
    }
    into("$buildDir/natives")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("-Djava.library.path=$buildDir/natives")
}

// Ensure that the build depends on the copyNatives task
tasks.getByName("build").dependsOn("copyNatives")

// Apparently this is invalid syntax but it still works and it needs to be here
application {
    mainClass.set("kengine.KengineKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}