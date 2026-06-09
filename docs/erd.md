# Diagram ERD — Bookstore

```mermaid
erDiagram
    users ||--o{ reservations : places
    books ||--o{ reservations : has

    users {
        bigserial id PK
        varchar email UK
        varchar password
        varchar role
        timestamp created_at
    }

    books {
        bigserial id PK
        varchar title
        varchar author
        varchar isbn UK
        int total_copies
        int available_copies
        timestamp created_at
    }

    reservations {
        bigserial id PK
        bigint user_id FK
        bigint book_id FK
        varchar status
        timestamp created_at
        timestamp returned_at
    }
```

## Relacje

- **users → reservations** — jeden użytkownik może mieć wiele rezerwacji
- **books → reservations** — jedna książka może mieć wiele rezerwacji

## Enumy

| Tabela | Pole | Wartości |
|--------|------|----------|
| `users` | `role` | `USER`, `ADMIN` |
| `reservations` | `status` | `PENDING`, `ACTIVE`, `RETURNED`, `CANCELLED` |
