# Збірка з кореня репозиторію: onlineSchool_backend (Render: Root Directory = )
# fix deploy
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY education-domain/pom.xml education-domain/
COPY education-application/pom.xml education-application/
COPY education-infrastructure/pom.xml education-infrastructure/
COPY education-web/pom.xml education-web/

RUN apk add --no-cache maven && mvn dependency:go-offline -B -pl education-web -am

COPY education-domain/src education-domain/src
COPY education-application/src education-application/src
COPY education-infrastructure/src education-infrastructure/src
COPY education-web/src education-web/src

RUN mvn -pl education-web -am package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/education-web/target/*.jar app.jar

EXPOSE 8080
# Явно передаємо порт: YAML у контейнері має бути коректним, але це гарантує підхоплення PORT на Railway.
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar --server.port=${PORT:-8080}"]