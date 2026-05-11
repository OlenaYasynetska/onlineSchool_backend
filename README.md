# Education Platform Backend

## Українська

REST API освітньої платформи: **Java 21**, **Spring Boot 3.2**, **Spring Security + JWT**, основні дані в **MySQL** (JPA + **Flyway**), чат — опційно в **MongoDB** (профіль **`mongo`**, БД `school_chat`). Фронтенд: **`onlineSchool_frontend`**.

Збірка **багатомодульна Maven**; запускабельний модуль — **`education-web`** (`com.education.web.EducationWebApplication`).

> **Застарілий код:** каталог `src/main/java/com/education/platform` у корені цього репозиторію **не входить** у `pom.xml` і **не** потрапляє в Docker-збірку. Не орієнтуйтеся на нього — актуальна реалізація в `education-*` модулях.

### Стек

| Компонент | Призначення |
|-----------|-------------|
| Spring Boot Web, Security, Validation | REST, захист ендпоїнтів |
| Spring Data JPA + MySQL Connector | доменна модель, транзакції |
| Flyway | міграції `education-web/src/main/resources/db/migration` |
| Spring Data MongoDB (опційно) | чат, якщо активовано профіль `mongo` і `MONGODB_URI` |
| JJWT 0.12 | access / refresh токени |
| Spring Mail | запрошення (див. `application-local.yml`) |
| springdoc-openapi | Swagger UI / OpenAPI |
| Lombok, MapStruct | зручність коду й мапінг DTO |
| JUnit 5, Spring Test, Testcontainers | тести |

### Модулі Maven

```
education-backend/          (parent pom)
├── education-domain/       # Доменні типи та порти (наприклад student) — розвиток DDD
├── education-application/ # Use case-и (залежить від domain)
├── education-infrastructure/ # JPA-адаптери до domain (наприклад student)
└── education-web/         # REST, security, Flyway, Mongo-чат, головний клас, resources
```

Більшість контролерів і JPA-сутностей зараз у **`education-web`** (`com.education.web.*`); модулі domain/application/infrastructure поступово підсилюють архітектуру.

### Пакети `education-web` (огляд)

| Пакет | Зміст |
|-------|--------|
| `auth` | логін / реєстрація, користувачі, JWT-фільтри, JPA-моделі авторизації |
| `chat` | API чату MongoDB (увімкнено лише з профілем `mongo`) |
| `config` | безпека, OpenAPI, CORS тощо |
| `homework` | домашні завдання (вчитель / учень), файли |
| `materials` | навчальні матеріали, PDF, Issuu |
| `schedule` | розклад (учень / учитель / адмін школи) |
| `schooladmin` | кабінет `ADMIN_SCHOOL` |
| `superadmin` | `SUPER_ADMIN` |
| `student` | загальні REST для студентів (`/api/students` тощо) |
| `teacher` | кабінет учителя |
| `mail`, `error`, `util` | пошта, `GlobalExceptionHandler`, допоміжні класи |

Усі публічні REST-шляхи зазвичай під префіксом **`/api/...`**.

### Дані: MySQL і MongoDB

- **MySQL** — обов’язково для роботи застосунку (`spring.datasource.*`). За замовчуванням у `application.yml`: хост `127.0.0.1`, БД `schools`, користувач/пароль `root`/`root` (змініть у проді через змінні середовища).
- **MongoDB** — **лише для чату**, якщо:
  - у змінних активовано профіль **`mongo`** (`SPRING_PROFILES_ACTIVE` містить `mongo`), і
  - задано **`MONGODB_URI`** (локально або Atlas).  
  Без профілю `mongo` авто-конфігурація Mongo вимикається (див. `application.yml`).

### Профілі Spring

| Профіль | Навіщо |
|---------|--------|
| `mongo` | Увімкнути чат на MongoDB (`education.chat.mongodb-enabled: true`). |
| `local` | Flyway `repair-on-migrate: true` — якщо були зміни вже застосованих міграцій і помилка checksum (лише дев, не для проду). |

Приклад локально з чатом: `SPRING_PROFILES_ACTIVE=local,mongo` (див. коментар у `application-local.yml`).

### Ролі (Spring Security)

Ті самі, що на фронті: `SUPER_ADMIN`, `ADMIN_SCHOOL`, `TEACHER`, `STUDENT`.

### Змінні середовища (важливі)

| Змінна | Опис |
|--------|------|
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD` | Підключення до MySQL |
| `JWT_SECRET` | Секрет JWT (мінімум ~32 символи в проді) |
| `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` | Терміни токенів (опційно) |
| `PORT` | Порт сервера (Railway тощо; також підхоплюється в `main`) |
| `MONGODB_URI` | MongoDB для чату (разом із профілем `mongo`) |
| `CORS_ALLOWED_ORIGINS`, `CORS_MERGE_LOCALHOST` | CORS |
| `FRONTEND_BASE_URL` | Лінки в листах (за замовч. `http://localhost:4200`) |
| `HOMEWORK_UPLOAD_DIR` | Каталог файлів ДЗ (у Docker зручно тримати на томі) |
| `SUPER_ADMIN_EMAIL`, `SUPER_ADMIN_PASSWORD` | Bootstrap суперадміна |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP (див. `application-local.yml`) |

### Запуск локально

**Вимоги:** JDK **21**, **Maven**, доступна **MySQL**. За потреби чату — **MongoDB** або Atlas і профіль **`mongo`**.

З кореня `onlineSchool_backend`:

```bash
mvn -pl education-web -am spring-boot:run
```

Або зібрати JAR і запустити:

```bash
mvn -pl education-web -am package -DskipTests
java -jar education-web/target/app.jar
```

- API: базово `http://localhost:8080/api/...`
- **Swagger UI** (springdoc): `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Тести:

```bash
mvn -pl education-web -am test
```

### Docker

З кореня `onlineSchool_backend` (де лежить `Dockerfile`):

```bash
docker build -t education-backend .
docker run -p 8080:8080 \
  -e MYSQL_HOST=... -e MYSQL_USER=... -e MYSQL_PASSWORD=... -e JWT_SECRET=... \
  education-backend
```

Для чату додайте `-e SPRING_PROFILES_ACTIVE=mongo` та `-e MONGODB_URI=...`. Деталі деплою (Railway, Vercel, змінні) — **[`DEPLOY.md`](../DEPLOY.md)**.

---

## English

REST backend for the education platform: **Java 21**, **Spring Boot 3.2**, **Spring Security + JWT**, primary data in **MySQL** (JPA + **Flyway**), optional **MongoDB** for chat (**`mongo`** Spring profile, `school_chat` database). Frontend: **`onlineSchool_frontend`**.

Maven is **multi-module**; the runnable module is **`education-web`** (`com.education.web.EducationWebApplication`).

> **Legacy folder:** `src/main/java/com/education/platform` at the repo root is **not** wired into the parent `pom.xml` and is **not** included in the Docker build. Ignore it—the live code lives under **`education-*`**.

### Stack

| Piece | Role |
|-------|------|
| Spring Boot Web, Security, Validation | REST API, secured endpoints |
| Spring Data JPA + MySQL | relational model |
| Flyway | migrations in `education-web/src/main/resources/db/migration` |
| Spring Data MongoDB (optional) | chat when profile **`mongo`** + **`MONGODB_URI`** |
| JJWT 0.12 | access / refresh tokens |
| Spring Mail | invitations (see `application-local.yml`) |
| springdoc-openapi | Swagger UI |
| Lombok, MapStruct | boilerplate reduction, DTO mapping |
| JUnit 5, Spring Test, Testcontainers | tests |

### Maven modules

```
education-backend/          (parent)
├── education-domain/       # Domain types & ports (e.g. student)
├── education-application/  # Use cases
├── education-infrastructure/ # JPA adapters into domain ports
└── education-web/         # REST, security, Flyway, Mongo chat, main class
```

Most controllers and JPA entities currently live in **`education-web`** (`com.education.web.*`).

### `education-web` packages (overview)

| Package | Role |
|---------|------|
| `auth` | login/register, users, JWT, auth-related JPA |
| `chat` | MongoDB chat API (only with **`mongo`**) |
| `config` | security, OpenAPI, CORS, etc. |
| `homework` | homework portal teacher/student, uploads |
| `materials` | study materials, PDF, Issuu |
| `schedule` | schedules (student / teacher / school admin) |
| `schooladmin` | `ADMIN_SCHOOL` APIs |
| `superadmin` | `SUPER_ADMIN` APIs |
| `student`, `teacher` | student REST, teacher dashboard |
| `mail`, `error`, `util` | mail, `GlobalExceptionHandler`, helpers |

HTTP APIs are typically under **`/api/**`**.

### Data: MySQL vs MongoDB

- **MySQL** is required (`spring.datasource.*`). Defaults in `application.yml` point at local `schools` DB—override in prod with env vars.
- **MongoDB** is **only for chat** when Spring profile **`mongo`** is active and **`MONGODB_URI`** is set. Without **`mongo`**, Mongo auto-configuration is excluded (see `application.yml`).

### Spring profiles

| Profile | Purpose |
|---------|---------|
| `mongo` | Enable chat on Mongo (`education.chat.mongodb-enabled: true`). |
| `local` | Flyway `repair-on-migrate: true` for dev checksum repair—**do not use in prod**. |

### Roles

`SUPER_ADMIN`, `ADMIN_SCHOOL`, `TEACHER`, `STUDENT` (aligned with the frontend).

### Environment variables (key)

| Variable | Purpose |
|----------|---------|
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD` | JDBC |
| `JWT_SECRET` | signing key (use a strong secret in prod) |
| `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` | optional token TTL |
| `PORT` | server port (PaaS) |
| `MONGODB_URI` | Mongo for chat (+ **`mongo`** profile) |
| `CORS_ALLOWED_ORIGINS`, `CORS_MERGE_LOCALHOST` | CORS |
| `FRONTEND_BASE_URL` | links in emails |
| `HOMEWORK_UPLOAD_DIR` | homework file directory |
| `SUPER_ADMIN_EMAIL`, `SUPER_ADMIN_PASSWORD` | bootstrap super-admin |
| `MAIL_*` | SMTP (see `application-local.yml`) |

### Run locally

**Requirements:** JDK **21**, **Maven**, **MySQL**. For chat: **MongoDB** + **`mongo`** profile.

From `onlineSchool_backend`:

```bash
mvn -pl education-web -am spring-boot:run
```

Jar:

```bash
mvn -pl education-web -am package -DskipTests
java -jar education-web/target/app.jar
```

- REST: `http://localhost:8080/api/...`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI:** `http://localhost:8080/v3/api-docs`

Tests:

```bash
mvn -pl education-web -am test
```

### Docker

From repo root (`Dockerfile`):

```bash
docker build -t education-backend .
docker run -p 8080:8080 \
  -e MYSQL_HOST=... -e MYSQL_USER=... -e MYSQL_PASSWORD=... -e JWT_SECRET=... \
  education-backend
```

Add `-e SPRING_PROFILES_ACTIVE=mongo` and `-e MONGODB_URI=...` for chat. Deployment notes: **[`DEPLOY.md`](../DEPLOY.md)**.
