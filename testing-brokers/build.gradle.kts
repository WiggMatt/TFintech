plugins {
    id("java")
}

group = "ru.matthew"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation("com.rabbitmq:amqp-client:5.21.0")
    implementation("org.apache.kafka:kafka-clients:3.7.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test {
    useJUnitPlatform()
}

// Добавляем задачу для сборки JMH тестов
tasks.register<JavaCompile>("jmhCompile") {
    source = fileTree("src/main/java")
    include("**/*.java")

    // Используем все зависимости runtimeClasspath
    classpath = sourceSets.main.get().runtimeClasspath

    // Новый способ задания директории
    destinationDirectory.set(file("build/classes/jmh"))

    // Указываем компилятору использовать аннотации
    options.compilerArgs = listOf("-proc:only")
}


// Задача для запуска JMH тестов
tasks.register<JavaExec>("runJmh") {
    dependsOn("jmhCompile")
    mainClass.set("org.openjdk.jmh.Main")
    classpath = files("build/classes/jmh") + sourceSets.main.get().runtimeClasspath

    jvmArgs = listOf(
        "-Dorg.slf4j.simpleLogger.defaultLogLevel=warn",
        "-Dorg.slf4j.simpleLogger.log.org.apache.kafka=warn",
        "-Dorg.slf4j.simpleLogger.log.kafka=warn"
    )

    args = listOf(
        // Указываем вывод в файл
        "-o", "build/benchmark_results.txt",  // Файл для результатов
        "-f", "1",                            // Один прогон
        "-i", "5",                            // 5 итераций измерения
        "-wi", "2"                            // 2 итерации прогрева
    )
}
