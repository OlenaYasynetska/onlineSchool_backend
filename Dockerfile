# Збірка з кореня репозиторію: onlineSchool_backend.
# На Railway: Root Directory = корінь цього репо (тут лежить pom.xml і education-*/).
# Використовуємо офіційний Maven + JDK 21 — apk maven на Alpine часто ламає збірку.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY education-domain/pom.xml education-domain/
COPY education-application/pom.xml education-application/
COPY education-infrastructure/pom.xml education-infrastructure/
COPY education-web/pom.xml education-web/

RUN mvn dependency:go-offline -B -pl education-web -am

COPY education-domain/src education-domain/src
COPY education-application/src education-application/src
COPY education-infrastructure/src education-infrastructure/src
COPY education-web/src education-web/src

RUN mvn -pl education-web -am package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

# Один fat-jar (finalName=app). Не використовувати *.jar — також є original-app.jar → COPY з кількома джерелами ламає збірку образу.
COPY --from=build /app/education-web/target/app.jar app.jar

# HomeworkUploadDirectoryInitializer створює app.homework-upload.dir (за замовч. uploads/homework → /app/uploads/…).
# Без chown користувач app не має права писати в /app → AccessDeniedException при старті.
RUN mkdir -p /app/uploads/homework && chown -R app:app /app

USER app

EXPOSE 8080
# Явно передаємо порт: YAML у контейнері має бути коректним, але це гарантує підхоплення PORT на Railway.
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-8080}"]