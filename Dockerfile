# Используем официальное изображение JDK
FROM openjdk:17-jdk-slim AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем build.gradle и другие файлы зависимостей
COPY currency-rate/build.gradle.kts settings.gradle.kts gradle.properties /app/

# Загружаем зависимости
RUN ./gradlew build --no-daemon \
.github/workflows
# Копируем весь исходный код
COPY currency-rate /app

# Собираем приложение
RUN ./gradlew bootJar

# Указываем команду для запуска приложения
FROM openjdk:17-jdk-slim

# Копируем собранный JAR файл из предыдущего этапа
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Открываем порт для приложения
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
