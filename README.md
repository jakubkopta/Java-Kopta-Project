# Bookstore

Aplikacja REST API do zarządzania księgarnią online. Użytkownicy mogą przeglądać i rezerwować książki, a administratorzy zarządzają zasobami i wypożyczeniami.

## Uruchomienie

### Wymagania

- Java 17+
- Docker i Docker Compose

### Konfiguracja środowiska

Skopiuj plik z przykładową konfiguracją i uzupełnij własne hasła:

```bash
cp .env.example .env
```

### Opcja 1 - tylko baza danych (lokalny development)

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

### Uwierzytelnianie (JWT)

**Rejestracja użytkownika:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"password123"}'
```

**Logowanie:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@bookstore.com","password":"admin123"}'
```

**Konto administratora (tworzone przy pierwszym starcie):**
- Email: `admin@bookstore.com`
- Hasło: `admin123`

W Swaggerze kliknij **Authorize** i wklej token: `Bearer <token>`

| Rola | Uprawnienia |
|------|-------------|
| USER | Przeglądanie książek, rezerwacje, zwroty |
| ADMIN | Zarządzanie książkami, podgląd wszystkich rezerwacji |

### Rezerwacje

| Metoda | Endpoint | Rola |
|--------|----------|------|
| POST | `/api/reservations` | USER |
| GET | `/api/reservations/my` | USER |
| GET | `/api/reservations` | ADMIN |
| POST | `/api/reservations/{id}/return` | USER (własne), ADMIN |

### Zatrzymanie

```bash
docker compose down
```

## Wzorzec projektowy i polimorfizm

Wyszukiwanie książek wykorzystuje wzorzec **Strategy** z polimorfizmem:

- `BookSearchStrategy` - wspólny interfejs
- `TitleSearchStrategy`, `AuthorSearchStrategy`, `IsbnSearchStrategy`, `AllFieldsSearchStrategy` - konkretne strategie
- `BookSearchContext` - wybiera strategię na podstawie parametru `type`

Przykład:
```
GET /api/books/search?q=java&type=TITLE
GET /api/books/search?q=tolkien&type=AUTHOR
GET /api/books/search?q=978&type=ISBN
GET /api/books/search?q=book&type=ALL
```

## Baza danych

Schemat jest zarządzany przez **Flyway** (`src/main/resources/db/migration/`).

Diagram ERD: [docs/erd.md](docs/erd.md)

| Tabela | Opis |
|--------|------|
| `users` | Użytkownicy (role: USER, ADMIN) |
| `books` | Książki w księgarni |
| `reservations` | Rezerwacje / wypożyczenia |

## Testy

```bash
./mvnw test
./mvnw verify
```

Raport pokrycia JaCoCo (wymagane ≥ 80%): `target/site/jacoco/index.html`

## Autor

Jakub Kopta
