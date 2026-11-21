# EduPlatform — учебная платформа на Spring Boot и Hibernate

Итоговый проект по дисциплине **«ORM-фреймворки для Java»**. Backend учебной платформы для управления пользователями, курсами, учебными материалами, домашними заданиями и тестированием. Основной акцент — проектирование JPA-модели, связи сущностей, Spring Data JPA и работа с PostgreSQL.

## Технологии

- Java 21, Maven
- Spring Boot 4.2.0, Spring Web MVC, Spring Data JPA
- Hibernate ORM 7.4.15.Final
- PostgreSQL 16, Docker Compose
- Jakarta Bean Validation, BCrypt для хеширования паролей
- JUnit (Jupiter), Mockito, Spring Boot Test для тестирования

## Возможности

- Регистрация студентов с проверкой данных и хешированием пароля.
- Создание и просмотр курсов, модулей и уроков.
- Запись студентов на курсы с проверкой роли и защитой от повторной записи.
- Создание домашних заданий, отправка решений и выставление оценок.
- Создание тестов, вопросов с одиночным и множественным выбором, вариантов ответов; автоматический расчёт оценки в процентах и сохранение результатов.
- Единая обработка ошибок REST API; валидация входных DTO.
- Демонстрационный режим с автоматическим заполнением отдельной базы данных.

Приложение предоставляет **REST API**; отдельный веб-интерфейс не реализован. Регистрация и роли существуют на уровне модели и сервисной логики, но полноценная аутентификация и разграничение доступа на HTTP-уровне не реализованы.

## Модель данных и архитектура

В проекте **15 JPA-сущностей** и **16 физических таблиц** (включая связующую `course_tag`):

`User`, `Profile`, `Course`, `Category`, `Tag`, `Module`, `Lesson`, `Enrollment`, `Assignment`, `Submission`, `Quiz`, `Question`, `AnswerOption`, `QuizSubmission`, `CourseReview`.

Примеры отношений:

- **Один к одному:** `User` — `Profile`.
- **Один ко многим:** `Course` — `Module`; `Module` — `Lesson`; `Quiz` — `Question`.
- **Многие ко многим:** `Course` — `Tag` через `course_tag`.
- Запись пользователя на курс представлена самостоятельной сущностью `Enrollment`, связывающей `User` и `Course`.

В связанных коллекциях используется ленивая загрузка (`LAZY`). `spring.jpa.open-in-view=false`: доступ к необходимым связанным данным организован внутри сервисного/транзакционного слоя, а HTTP-ответы формируются через DTO.

Код разделён на пакеты:

```text
src/main/java/ru/skillfactory/eduplatform/
├── entity/       # JPA-сущности и enum ролей
├── repository/   # Spring Data JPA
├── service/      # бизнес-логика и транзакции
├── controller/   # REST API
├── dto/          # запросы и ответы API
└── config/       # конфигурация и демонстрационные данные
```

## Требования для запуска

- JDK 21
- Maven (или Maven Wrapper `mvnw.cmd`)
- Docker Desktop с поддержкой Docker Compose (для PostgreSQL)
- Свободные порты **5434** для PostgreSQL и **8080** для приложения

Команды ниже приведены для PowerShell из **корневой папки проекта**.

### 1. Настройка окружения

Создайте в корне проекта файл `.env`:

```properties
DB_PASSWORD=your_local_password
```

Приложение использует настройки из `src/main/resources/application.properties`. По умолчанию JDBC URL — `jdbc:postgresql://localhost:5434/eduplatform`, пользователь — `eduplatform`, пароль — из `DB_PASSWORD`.

**Не публикуйте `.env` в Git:** файл исключён через `.gitignore`.

### 2. Запуск PostgreSQL

```powershell
docker compose up -d postgres
```

Compose использует образ `postgres:16`, контейнер `eduplatform-postgres`, порт `127.0.0.1:5434` и именованный том `postgres_data`. При первом запуске автоматически создаётся основная БД `eduplatform`. Схему таблиц Hibernate создаёт/обновляет на основе JPA-модели (`ddl-auto=update`).

### 3. Запуск REST API

```powershell
mvn spring-boot:run
```

Сервер доступен на `http://localhost:8080`. Для остановки используйте `Ctrl+C`.

## Демонстрационные данные

Для демонстрационного режима используется **отдельная база** `eduplatform_demo`. Создайте её один раз, когда PostgreSQL запущен:

```powershell
docker exec eduplatform-postgres psql -U eduplatform -d postgres -c "CREATE DATABASE eduplatform_demo;"
```

Если база уже существует, повторно создавать её не требуется.

Запустите приложение с профилем `demo`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Настройки режима находятся в `src/main/resources/application-demo.properties`. `DemoDataLoader` автоматически создаёт преподавателя, двух студентов, категорию, курс «Основы Java», модуль, урок, домашнее задание, записи студентов на курс, решение, тест с двумя вопросами и вариантами ответов, результат теста. При повторном запуске загрузка пропускается, если демонстрационный преподаватель уже существует.

**Демонстрационные учётные записи** (только для локального знакомства с данными):

| Роль | Email | Пароль |
|---|---|---|
| Преподаватель | `demo.teacher@example.com` | `Demo12345!` |
| Студент | `demo.student@example.com` | `Demo12345!` |
| Студент | `demo.student2@example.com` | `Demo12345!` |

Значения паролей хранятся в БД в виде BCrypt-хешей. Демонстрационные записи не предназначены для публичного сервиса.

## REST API

Базовый URL: `http://localhost:8080`. Запросы `POST`/`PATCH` используют JSON (`Content-Type: application/json`). ID в примерах условны и зависят от содержимого базы.

| Метод | Путь | Назначение |
|---|---|---|
| `POST` | `/api/users/register` | Регистрация студента |
| `POST` | `/api/courses` | Создание курса |
| `GET` | `/api/courses` | Список курсов |
| `GET` | `/api/courses/{id}` | Курс по ID |
| `POST` | `/api/courses/{id}/modules` | Добавление модуля |
| `GET` | `/api/courses/{id}/modules` | Модули курса |
| `POST` | `/api/modules/{moduleId}/lessons` | Создание урока |
| `GET` | `/api/modules/{moduleId}/lessons` | Уроки модуля |
| `POST` | `/api/enrollments` | Запись студента на курс |
| `GET` | `/api/enrollments/students/{userId}` | Записи студента |
| `GET` | `/api/enrollments/courses/{courseId}` | Записи на курс |
| `POST` | `/api/lessons/{lessonId}/assignments` | Создание домашнего задания |
| `GET` | `/api/lessons/{lessonId}/assignments` | Задания урока |
| `POST` | `/api/assignments/{assignmentId}/submissions` | Отправка решения |
| `PATCH` | `/api/submissions/{submissionId}/grade` | Оценивание решения |
| `POST` | `/api/modules/{moduleId}/quizzes` | Создание теста |
| `GET` | `/api/modules/{moduleId}/quizzes` | Тесты модуля |
| `POST` | `/api/quizzes/{quizId}/questions` | Добавление вопроса |
| `POST` | `/api/questions/{questionId}/options` | Добавление варианта ответа |
| `POST` | `/api/quizzes/{quizId}/submissions` | Прохождение теста и получение оценки |

### Примеры JSON-запросов

**Регистрация:** `POST /api/users/register`

```json
{
  "firstName": "Анна",
  "lastName": "Иванова",
  "email": "anna@example.com",
  "password": "StrongPass123"
}
```

**Запись на курс:** `POST /api/enrollments`

```json
{
  "studentId": 1,
  "courseId": 1
}
```

**Отправка домашнего задания:** `POST /api/assignments/1/submissions`

```json
{
  "studentId": 1,
  "content": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }"
}
```

**Прохождение теста:** `POST /api/quizzes/1/submissions`

```json
{
  "studentId": 1,
  "answers": {
    "1": [1],
    "2": [4, 6]
  }
}
```

В поле `answers` ключ — ID вопроса, значение — список ID выбранных вариантов. Тест оценивается по доле полностью правильно отвеченных вопросов; для `MULTIPLE_CHOICE` набор ответов должен совпасть с правильным полностью.

Успешное создание ресурсов возвращает `201 Created`. Некорректные данные и нарушения бизнес-правил обрабатываются REST-обработчиком ошибок.

## Автоматические тесты

Реализовано **40 автотестов**, успешно выполненных командой `mvn clean verify`:

- 34 модульных теста сервисов (`UserService`, `CourseService`, `EnrollmentService`, `AssignmentService`, `QuizService`) с JUnit и Mockito;
- 5 интеграционных тестов репозиториев и CRUD через настоящий PostgreSQL;
- 1 тест загрузки контекста Spring Boot.

Интеграционные тесты используют профиль `test` и **отдельную базу** `eduplatform_test`. Перед первым запуском тестов создайте её:

```powershell
docker exec eduplatform-postgres psql -U eduplatform -d postgres -c "CREATE DATABASE eduplatform_test;"
```

Настройки тестовой базы: `src/test/resources/application-test.properties`. Для неё Hibernate создаёт тестовую схему (`ddl-auto=create-drop`); изменения интеграционных тестов выполняются с откатом транзакций. **Не направляйте тестовый профиль на основную базу `eduplatform`**.

Запуск всех тестов:

```powershell
mvn clean test
```

Полная сборка и проверка (включая исполняемый JAR):

```powershell
mvn clean verify
```

Исполняемый JAR появляется в `target/eduplatform-0.0.1-SNAPSHOT.jar`. Для работы тестов Spring Boot запускать отдельно не требуется; PostgreSQL должен быть доступен для интеграционных проверок.

## Остановка окружения

```powershell
docker compose stop
```

При остановке контейнера PostgreSQL данные сохраняются в именованном томе. **Не используйте `docker compose down -v`, если данные необходимо сохранить.**

## Статус автоматизации

В проекте уже настроены Maven и Docker Compose для PostgreSQL. Dockerfile для приложения и GitHub Actions CI рассматриваются как отдельный завершающий этап; до их добавления запуск приложения и тестов выполняется командами выше.