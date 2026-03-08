# Education Platform Backend

Backend для образовательной платформы: **Java 21**, **Spring Boot 3**, **MongoDB**, **JWT + Spring Security**.

## Стек

- **Spring Boot 3.2** — Web, Data MongoDB, Security, Validation
- **MongoDB** — база данных
- **JWT (JJWT)** — аутентификация
- **Lombok**, **MapStruct** — упрощение кода и маппинг
- **Swagger / OpenAPI 3** — документация API
- **JUnit 5**, **Testcontainers** — тесты

## Роли

- `SUPER_ADMIN` — суперадмин
- `ADMIN_SCHOOL` — администратор школы
- `TEACHER` — учитель
- `STUDENT` — ученик

## Запуск локально

1. **MongoDB** — локально или MongoDB Atlas (переменная `MONGODB_URI`).
2. Переменные (опционально):
   - `MONGODB_URI` — URI MongoDB (по умолчанию `mongodb://localhost:27017/education_platform`)
   - `JWT_SECRET` — секрет для JWT (не менее 32 символов)
   - `PORT` — порт (по умолчанию 8080)

```bash
./mvnw spring-boot:run
```

API: `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## Docker

```bash
docker build -t education-backend .
docker run -p 8080:8080 -e MONGODB_URI=... -e JWT_SECRET=... education-backend
```

## Production

Профиль `prod`: `spring.profiles.active=prod`.  
Обязательные переменные: `MONGODB_URI`, `JWT_SECRET`.

## Структура проекта

```
src/main/java/com/education/platform/
├── config/          # Security, MongoDB, OpenAPI
├── controller/      # Auth, Student, Teacher
├── service/
├── repository/
├── model/           # User, School, Student, Teacher, Course, Lesson, Assignment
├── dto/request, dto/response
├── mapper/          # MapStruct
├── exception/       # GlobalExceptionHandler
└── util/            # JwtUtil
```

## CI/CD (рекомендация)

- **GitHub Actions**: сборка → Docker image → деплой на AWS (EC2/ECS).
- **Frontend**: Angular → S3 + CloudFront.
- **БД**: MongoDB Atlas.


education-backend (onlineSchool_backend)
├── src/main/java/com/education/platform/
│   ├── config/
│   │   ├── security/     → SecurityConfig, JwtAuthenticationFilter
│   │   ├── database/     → MongoConfig
│   │   └── OpenApiConfig
│   ├── controller/      → AuthController, StudentController, TeacherController
│   ├── service/         → AuthService, StudentService, TeacherService
│   ├── repository/      → User, Student, Teacher, School, Course, Lesson, Assignment
│   ├── model/           → User, School, Student, Teacher, Course, Lesson, Assignment + Role
│   ├── dto/request & response
│   ├── mapper/          → UserMapper, StudentMapper, TeacherMapper (MapStruct)
│   ├── exception/       → GlobalExceptionHandler, ResourceNotFoundException, ErrorBody
│   └── util/            → JwtUtil
├── src/main/resources/
│   ├── application.yml
│   └── application-prod.yml
├── Dockerfile
├── pom.xml
├── .gitignore
└── README.md