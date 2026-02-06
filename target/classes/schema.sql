CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS todo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL DEFAULT 1,
    author VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    detail VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT NOT NULL DEFAULT 1,
    deadline DATE,
    CONSTRAINT fk_todo_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_todo_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS todo_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    todo_id BIGINT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachment_todo FOREIGN KEY (todo_id) REFERENCES todo(id)
);

CREATE TABLE IF NOT EXISTS todo_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    todo_id BIGINT NOT NULL,
    editor_user_id BIGINT NOT NULL,
    edited_at TIMESTAMP NOT NULL,
    note VARCHAR(200) NOT NULL,
    CONSTRAINT fk_history_todo FOREIGN KEY (todo_id) REFERENCES todo(id),
    CONSTRAINT fk_history_user FOREIGN KEY (editor_user_id) REFERENCES users(id)
);
