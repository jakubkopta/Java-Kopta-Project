# Zrzuty ekranu do dokumentacji

Umieść w tym folderze następujące pliki PNG (wymagane w README):

| Plik | Co zrobić screen |
|------|------------------|
| `erd.png` | Diagram ERD z [dbdiagram.io](https://dbdiagram.io) (import `docs/erd.dbml`) lub eksport z `docs/erd.md` |
| `swagger.png` | Swagger UI: `http://localhost:8080/swagger-ui.html` |
| `login.png` | Odpowiedź `POST /api/auth/login` w Swaggerze z tokenem JWT |
| `books-list.png` | `GET /api/books` — lista książek |
| `reservations.png` | `GET /api/reservations/my` lub panel rezerwacji |
| `jacoco-coverage.jpg` | Raport po `./mvnw verify` → `target/site/jacoco/index.html` |

## Jak zrobić screen

1. Uruchom aplikację (`docker compose up -d db` + `./mvnw spring-boot:run`)
2. Otwórz Swagger lub JaCoCo w przeglądarce
3. Zrób zrzut ekranu (macOS: `Cmd + Shift + 4`)
4. Zapisz pliki z powyższymi nazwami w tym folderze
