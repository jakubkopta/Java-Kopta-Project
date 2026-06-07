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

### Zatrzymanie

```bash
docker compose down
```

## Autor

Jakub Kopta
