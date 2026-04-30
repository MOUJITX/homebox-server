# Database Schema

## Overview

The application uses MySQL and manages schema via Hibernate's `ddl-auto: update` strategy. Tables are created automatically on first startup.

## Tables

### roles

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(255) | UNIQUE, NOT NULL         |
| description | VARCHAR(255) | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### users

| Column                | Type         | Constraints              |
|-----------------------|--------------|--------------------------|
| id                    | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| username              | VARCHAR(255) | UNIQUE, NOT NULL         |
| password              | VARCHAR(255) | NOT NULL (BCrypt hash)   |
| display_name          | VARCHAR(255) | NULLABLE                 |
| role_id               | BIGINT       | NOT NULL, FK → roles(id) |
| force_change_password | BIT(1)       | NOT NULL, default TRUE   |
| created_at            | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at            | DATETIME(6)  | NOT NULL, auto-set       |

### good_categories

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(255) | UNIQUE, NOT NULL         |
| description | VARCHAR(255) | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### good_brands

| Column       | Type         | Constraints              |
|--------------|--------------|--------------------------|
| id           | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| brand_name   | VARCHAR(255) | UNIQUE, NOT NULL         |
| company_name | VARCHAR(255) | NULLABLE                 |
| created_at   | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at   | DATETIME(6)  | NOT NULL, auto-set       |

### goods

| Column             | Type         | Constraints                       |
|--------------------|--------------|-----------------------------------|
| id                 | BIGINT       | PRIMARY KEY, AUTO_INCREMENT       |
| product_name       | VARCHAR(255) | NOT NULL                          |
| barcode            | VARCHAR(255) | UNIQUE, NOT NULL                  |
| category_id        | BIGINT       | NOT NULL, FK → good_categories(id)|
| brand_id           | BIGINT       | NOT NULL, FK → good_brands(id)    |
| expiring_soon_days | INT          | NOT NULL, default 30              |
| created_at         | DATETIME(6)  | NOT NULL, auto-set                |
| updated_at         | DATETIME(6)  | NOT NULL, auto-set                |

### good_items

| Column          | Type         | Constraints              |
|-----------------|--------------|--------------------------|
| id              | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| good_id         | BIGINT       | NOT NULL, FK → goods(id) |
| product_date    | DATE         | NOT NULL                 |
| expiration_date | DATE         | NOT NULL                 |
| life_days       | INT          | NOT NULL                 |
| in_use          | BIT(1)       | NOT NULL, default TRUE   |
| created_at      | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at      | DATETIME(6)  | NOT NULL, auto-set       |

### file_records

| Column            | Type         | Constraints              |
|-------------------|--------------|--------------------------|
| id                | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| stored_filename   | VARCHAR(255) | NOT NULL (relative path: `yyyymmdd/uuid.ext`) |
| original_filename | VARCHAR(255) | NOT NULL (original upload name) |
| content_type      | VARCHAR(255) | NOT NULL                 |
| file_size         | BIGINT       | NOT NULL                 |
| created_at        | DATETIME(6)  | NOT NULL, auto-set       |

### good_pictures

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| good_id | BIGINT | NOT NULL, FK → goods(id)     |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

## Relationships

- `users.role_id` → `roles.id` (Many-to-One: many users can share one role)
- `goods.category_id` → `good_categories.id` (Many-to-One: many goods can share one category)
- `goods.brand_id` → `good_brands.id` (Many-to-One: many goods can share one brand)
- `good_items.good_id` → `goods.id` (Many-to-One: many items belong to one good)
- `good_pictures.good_id` → `goods.id` (Many-to-One: many pictures belong to one good)
- `good_pictures.file_id` → `file_records.id` (Many-to-One: many pictures can reference one file)

## Initial Data

On first startup, the application seeds:
- **Roles**: `root` (System administrator role), `member` (Standard member role)
- **Users**: One root user with credentials from environment variables (`ROOT_USERNAME`, `ROOT_PASSWORD`) with `force_change_password = true`
