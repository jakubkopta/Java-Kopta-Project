CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT users_role_check CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE books (
    id               BIGSERIAL    PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    total_copies     INT          NOT NULL,
    available_copies INT          NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT books_total_copies_check CHECK (total_copies >= 0),
    CONSTRAINT books_available_copies_check CHECK (available_copies >= 0),
    CONSTRAINT books_available_lte_total_check CHECK (available_copies <= total_copies)
);

CREATE TABLE reservations (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    book_id     BIGINT      NOT NULL REFERENCES books (id),
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    returned_at TIMESTAMP,
    CONSTRAINT reservations_status_check CHECK (status IN ('PENDING', 'ACTIVE', 'RETURNED', 'CANCELLED'))
);

CREATE INDEX idx_reservations_user_id ON reservations (user_id);
CREATE INDEX idx_reservations_book_id ON reservations (book_id);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author ON books (author);
