// Модуль «ПОС» (ДЗ-2). Подключается в корневом settings.gradle.kts:
//     include("modules:second_homework")
// Репозиторий mavenCentral и group задаёт корневой build.gradle.kts.

plugins {
    java
    application
    jacoco
    checkstyle
}

version = "1.0-SNAPSHOT"

dependencies {
    // Только DI-контейнер, без Spring Boot: приложению не нужны
    // автоконфигурация и встроенный логгер.
    implementation("org.springframework:spring-context:7.0.10")

    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "app.Main"
}

tasks.withType<JavaCompile>().configureEach {
    // Кириллица в строках и Javadoc не зависит от кодировки ОС.
    options.encoding = "UTF-8"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    systemProperty("stdout.encoding", "UTF-8")
    systemProperty("stderr.encoding", "UTF-8")
    systemProperty(
        "pos.export.dir",
        layout.buildDirectory.dir("exports").get().asFile.absolutePath,
    )
}

tasks.test {
    useJUnitPlatform()
    // Тесты контейнера не должны писать файлы в каталог модуля.
    systemProperty(
        "pos.export.dir",
        layout.buildDirectory.dir("test-exports").get().asFile.absolutePath,
    )
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.15"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Конфигурация лежит в модуле: config/checkstyle/checkstyle.xml
// и suppressions.xml рядом с ней (${config_loc} Gradle задаёт сам).
checkstyle {
    toolVersion = "14.1.0"
    configDirectory = layout.projectDirectory.dir("config/checkstyle")
    configFile = file("config/checkstyle/checkstyle.xml")
    isShowViolations = true
    isIgnoreFailures = false
    maxWarnings = 0
}
