# Bookstore

Aplikacja REST API do zarządzania księgarnią online. Użytkownicy mogą przeglądać i rezerwować książki, a administratorzy zarządzają zasobami i wypożyczeniami.
## Autor
Jakub Kopta

## Spis treści

- [Technologie](#technologie)
- [Funkcjonalności](#funkcjonalności)
- [Uruchomienie](#uruchomienie)
- [API](#api)
- [Uwierzytelnianie i role](#uwierzytelnianie-i-role)
- [Baza danych](#baza-danych)
- [Wzorzec projektowy i polimorfizm](#wzorzec-projektowy-i-polimorfizm)
- [Testy](#testy)
- [Zrzuty ekranu](#zrzuty-ekranu)

## Technologie


| Technologia                 | Zastosowanie                          |
| --------------------------- | ------------------------------------- |
| Java 17                     | Język programowania                   |
| Spring Boot 4               | Framework aplikacji                   |
| Spring Security + JWT       | Uwierzytelnianie i autoryzacja (RBAC) |
| Spring Data JPA + Hibernate | Mapowanie obiektowo-relacyjne         |
| PostgreSQL                  | Baza danych                           |
| Flyway                      | Migracje schematu bazy                |
| Docker + Docker Compose     | Konteneryzacja                        |
| Springdoc OpenAPI           | Dokumentacja API (Swagger UI)         |
| JUnit 5 + Mockito           | Testy                                 |
| JaCoCo                      | Analiza pokrycia kodu         |
| Maven                       | Zarządzanie zależnościami i buildem   |
| Lombok                      | Redukcja boilerplate w encjach        |


## Funkcjonalności

### Użytkownik (USER)

- Rejestracja i logowanie
- Przeglądanie i wyszukiwanie książek
- Rezerwacja książek
- Podgląd własnych rezerwacji
- Zwrot wypożyczonych książek

### Administrator (ADMIN)

- Zarządzanie książkami (dodawanie, edycja, usuwanie)
- Podgląd wszystkich rezerwacji
- Zwrot książek w imieniu użytkowników

## Uruchomienie

### Wymagania

- Java 17+
- Docker i Docker Compose
- Maven 3.9+ (lub `./mvnw` z repozytorium)

### Konfiguracja środowiska

```bash
cp .env.example .env
```

### Opcja 1 - lokalny development (zalecane)

```bash
docker compose up -d db
./mvnw spring-boot:run
```

### Opcja 2 - cała aplikacja w Dockerze

```bash
docker compose up --build
```

Aplikacja: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Zatrzymanie

```bash
docker compose down
```

## API

### Autentykacja


| Metoda | Endpoint             | Dostęp    |
| ------ | -------------------- | --------- |
| POST   | `/api/auth/register` | Publiczny |
| POST   | `/api/auth/login`    | Publiczny |


### Książki


| Metoda | Endpoint                     | Dostęp    |
| ------ | ---------------------------- | --------- |
| GET    | `/api/books`                 | Publiczny |
| GET    | `/api/books/{id}`            | Publiczny |
| GET    | `/api/books/search?q=&type=` | Publiczny |
| POST   | `/api/books`                 | ADMIN     |
| PUT    | `/api/books/{id}`            | ADMIN     |
| DELETE | `/api/books/{id}`            | ADMIN     |


Parametr `type` w wyszukiwaniu: `TITLE`, `AUTHOR`, `ISBN`, `ALL`.

### Rezerwacje


| Metoda | Endpoint                        | Dostęp               |
| ------ | ------------------------------- | -------------------- |
| POST   | `/api/reservations`             | USER                 |
| GET    | `/api/reservations/my`          | USER                 |
| GET    | `/api/reservations`             | ADMIN                |
| POST   | `/api/reservations/{id}/return` | USER (własne), ADMIN |


## Uwierzytelnianie i role

Aplikacja używa **JWT** (Bearer token). Po zalogowaniu skopiuj token i użyj go w Swaggerze (**Authorize** → wklej sam token, bez słowa `Bearer`).

**Konto administratora** (tworzone przy pierwszym starcie):


| Pole  | Wartość               |
| ----- | --------------------- |
| Email | `admin@bookstore.com` |
| Hasło | `admin123`            |


**Przykład logowania:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'
```

**Przykład rejestracji użytkownika:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"newuser@example.com","password":"password123"}'
```

### Fixtury

Przy **pierwszym starcie na pustej bazie** aplikacja ładuje przykładowe dane (`DataInitializer`):

| Konto | Hasło | Rola |
| ----- | ----- | ---- |
| `admin@bookstore.com` | `admin123` | ADMIN |
| `user@example.com` | `user123` | USER |

Dodatkowo: **20 przykładowych książek** (bez rezerwacji).

Aby przeładować fixtury od zera:

```bash
docker compose down -v
docker compose up -d db
./mvnw spring-boot:run
```

## Baza danych

Schemat jest zarządzany przez **Flyway** (`src/main/resources/db/migration/`).

Szczegółowy diagram ERD: [docs/erd.md](docs/erd.md)


| Tabela         | Opis                            |
| -------------- | ------------------------------- |
| `users`        | Użytkownicy (role: USER, ADMIN) |
| `books`        | Książki w księgarni             |
| `reservations` | Rezerwacje / wypożyczenia       |


### Diagram ERD

![Diagram ERD](/docs/screenshots/erd.png)

## Wzorzec projektowy i polimorfizm

Wyszukiwanie książek zrealizowano wzorcem **Strategy** z wykorzystaniem **polimorfizmu**.

**Interfejs:** `BookSearchStrategy` - definiuje metodę `search(String query)`.

**Implementacje:**

- `TitleSearchStrategy` - wyszukiwanie po tytule
- `AuthorSearchStrategy` - wyszukiwanie po autorze
- `IsbnSearchStrategy` - wyszukiwanie po ISBN
- `AllFieldsSearchStrategy` - wyszukiwanie we wszystkich polach

**Kontekst:** `BookSearchContext` - wybiera strategię w runtime na podstawie parametru `type` z requestu.

```
GET /api/books/search?q=java&type=TITLE   → TitleSearchStrategy
GET /api/books/search?q=tolkien&type=AUTHOR → AuthorSearchStrategy
```

Polimorfizm polega na tym, że `BookSearchContext` wywołuje `search()` na obiekcie typu `BookSearchStrategy`, nie wiedząc z góry, która konkretna implementacja zostanie użyta.

## Testy

```bash
./mvnw test          # uruchomienie testów
./mvnw verify        # testy + raport JaCoCo
```

Raport HTML: `target/site/jacoco/index.html`

```bash
open target/site/jacoco/index.html
```

## Zrzuty ekranu

### Raport pokrycia testów JaCoCo

![Raport JaCoCo](/docs/screenshots/jacoco-coverage.jpg)

### Swagger UI

![Swagger UI](/docs/screenshots/swagger.png)

### Logowanie (JWT)

![Logowanie](/docs/screenshots/login.png)

### Lista książek

![Lista książek](/docs/screenshots/books-list.png)

### Rezerwacje

![Rezerwacje](/docs/screenshots/reservations.png)