# Bước 1: Build ứng dụng bằng Gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN ./gradlew build -x test --no-daemon

# Bước 2: Chạy ứng dụng bằng Amazon Corretto 17
FROM amazoncorretto:17-alpine
WORKDIR /app
# Lệnh copy này lấy file jar trong build/libs
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]