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
| extract_status    | VARCHAR(16)  | NOT NULL, default PENDING |
| chunk_status      | VARCHAR(16)  | NOT NULL, default PENDING |
| created_at        | DATETIME(6)  | NOT NULL, auto-set       |

### text_chunks

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| file_id     | BIGINT       | NOT NULL, FK → file_records(id) |
| chunk_index | INT          | NOT NULL                 |
| chunk_text  | MEDIUMTEXT   | NOT NULL                 |
| page_number | INT          | NULLABLE                 |
| token_count | INT          | NULLABLE                 |
| indexed     | BIT(1)       | NOT NULL, default FALSE  |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |

UNIQUE INDEX on (file_id, chunk_index).

### good_pictures

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| good_id | BIGINT | NOT NULL, FK → goods(id)     |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### good_attachments

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| good_id | BIGINT | NOT NULL, FK → goods(id)     |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### asset_categories

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(255) | UNIQUE, NOT NULL         |
| description | VARCHAR(255) | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### asset_places

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(255) | UNIQUE, NOT NULL         |
| description | VARCHAR(255) | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### asset_stores

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(255) | NOT NULL                 |
| channel     | VARCHAR(255) | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### assets

| Column           | Type           | Constraints                          |
|------------------|----------------|--------------------------------------|
| id               | BIGINT         | PRIMARY KEY, AUTO_INCREMENT          |
| name             | VARCHAR(255)   | NOT NULL                             |
| barcode          | VARCHAR(255)   | NULLABLE                             |
| serial_number    | VARCHAR(255)   | NULLABLE                             |
| category_id      | BIGINT         | NOT NULL, FK → asset_categories(id)  |
| place_id         | BIGINT         | NOT NULL, FK → asset_places(id)      |
| in_use           | BIT(1)         | NOT NULL, default TRUE               |
| retire_date      | DATE           | NULLABLE (required when in_use=FALSE, must not be future) |
| price            | DECIMAL(19,2)  | NULLABLE                             |
| shop_date        | DATE           | NULLABLE                             |
| store_id         | BIGINT         | NULLABLE, FK → asset_stores(id)      |
| has_warranty     | BIT(1)         | NOT NULL, default FALSE            |
| active_date      | DATE           | NULLABLE                           |
| warranty_period  | INT            | NULLABLE (days)                    |
| expiration_date  | DATE           | NULLABLE                           |
| parent_id        | BIGINT         | NULLABLE, FK → assets(id)          |
| note             | TEXT           | NULLABLE                           |
| created_at       | DATETIME(6)    | NOT NULL, auto-set                 |
| updated_at       | DATETIME(6)    | NOT NULL, auto-set                 |

UNIQUE INDEX on (barcode, serial_number) — enforced only when both are non-null.

### asset_pictures

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| asset_id| BIGINT | NOT NULL, FK → assets(id)    |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### asset_attachments

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| asset_id| BIGINT | NOT NULL, FK → assets(id)    |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### invoices

| Column          | Type           | Constraints                          |
|-----------------|----------------|--------------------------------------|
| id              | BIGINT         | PRIMARY KEY, AUTO_INCREMENT          |
| invoice_number  | VARCHAR(255)   | NULLABLE, UNIQUE                     |
| invoice_date    | DATE           | NULLABLE                             |
| invoice_type    | VARCHAR(50)    | NOT NULL                             |
| invoice_status  | VARCHAR(50)    | NOT NULL, default NORMAL             |
| seller_name     | VARCHAR(255)   | NULLABLE                             |
| seller_tax_id   | VARCHAR(255)   | NULLABLE                             |
| buyer_name      | VARCHAR(255)   | NULLABLE                             |
| buyer_tax_id    | VARCHAR(255)   | NULLABLE                             |
| amount          | DECIMAL(19,2)  | NULLABLE                             |
| tax_amount      | DECIMAL(19,2)  | NULLABLE                             |
| total_amount    | DECIMAL(19,2)  | NOT NULL                             |
| remark          | TEXT           | NULLABLE                             |
| file_id         | BIGINT         | NULLABLE, FK → file_records(id)      |
| preview_image   | LONGTEXT       | NULLABLE (base64 PNG of first page)  |
| created_at      | DATETIME(6)    | NOT NULL, auto-set                   |
| updated_at      | DATETIME(6)    | NOT NULL, auto-set                   |

### invoice_attachments

| Column      | Type   | Constraints                      |
|-------------|--------|----------------------------------|
| id          | BIGINT | PRIMARY KEY, AUTO_INCREMENT      |
| invoice_id  | BIGINT | NOT NULL, FK → invoices(id)      |
| file_id     | BIGINT | NOT NULL, FK → file_records(id)  |

### asset_invoices

| Column      | Type        | Constraints                          |
|-------------|-------------|--------------------------------------|
| id          | BIGINT      | PRIMARY KEY, AUTO_INCREMENT          |
| asset_id    | BIGINT      | NOT NULL, FK → assets(id)            |
| invoice_id  | BIGINT      | NOT NULL, FK → invoices(id)          |
| created_at  | DATETIME(6) | NOT NULL, auto-set                   |

UNIQUE INDEX on (asset_id, invoice_id) — prevents duplicate bindings.

### system_config

| Column       | Type         | Constraints              |
|--------------|--------------|--------------------------|
| id           | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| config_key   | VARCHAR(100) | UNIQUE, NOT NULL         |
| config_value | TEXT         | NULLABLE                 |
| config_group | VARCHAR(50)  | NOT NULL                 |
| is_sensitive | BIT(1)       | NOT NULL, default FALSE  |
| description  | VARCHAR(255) | NULLABLE                 |
| created_at   | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at   | DATETIME(6)  | NOT NULL, auto-set       |

Stores key-value configuration pairs grouped by category (`qiniu`, `ai`, `notification`, `elasticsearch`). Seeded on first startup from environment variables. Configurable at runtime via the Settings UI (root only).

Elasticsearch key: `elasticsearch.enabled` (`"true"` / `"false"`, default `"false"`). Host and port are configured via `ES_HOST` / `ES_PORT` environment variables (spring.elasticsearch.uris). When disabled, search features are hidden in the UI and ES indexing is skipped.

### notifications

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| type        | VARCHAR(30)  | NOT NULL (ITEM_EXPIRING, ITEM_EXPIRED, WARRANTY_EXPIRING, WARRANTY_EXPIRED, MEDICATION_REMINDER) |
| title       | VARCHAR(200) | NOT NULL                 |
| content     | TEXT         | NOT NULL                 |
| is_read     | BIT(1)       | NOT NULL, default FALSE  |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| read_at     | DATETIME(6)  | NULLABLE                 |
| source_type | VARCHAR(20)  | NULLABLE (GOOD, ASSET, or MEDICATION) |
| source_id   | BIGINT       | NULLABLE (good.id or asset.id) |
| notify_date | DATE         | NULLABLE |

### medication_reminders

| Column             | Type         | Constraints                          |
|--------------------|--------------|--------------------------------------|
| id                 | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| good_id            | BIGINT       | NOT NULL, FK → goods(id)             |
| dosage_method      | VARCHAR(50)  | NULLABLE                             |
| dosage_quantity    | VARCHAR(50)  | NULLABLE                             |
| dosage_unit        | VARCHAR(50)  | NULLABLE                             |
| dosage_note        | VARCHAR(255) | NULLABLE                             |
| frequency_hours    | VARCHAR(100) | NOT NULL                             |
| course_start_date  | DATE         | NOT NULL                             |
| course_end_date    | DATE         | NOT NULL                             |
| enabled            | BIT(1)       | NOT NULL, default TRUE               |
| created_at         | DATETIME(6)  | NOT NULL, auto-set                   |
| updated_at         | DATETIME(6)  | NOT NULL, auto-set                   |

UNIQUE INDEX `uk_medication_course` on (good_id, course_start_date, course_end_date).

### good_attachments

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| good_id | BIGINT | NOT NULL, FK → goods(id)     |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### asset_attachments

| Column  | Type   | Constraints                  |
|---------|--------|------------------------------|
| id      | BIGINT | PRIMARY KEY, AUTO_INCREMENT  |
| asset_id| BIGINT | NOT NULL, FK → assets(id)    |
| file_id | BIGINT | NOT NULL, FK → file_records(id) |

### medical_institutions

| Column      | Type         | Constraints              |
|-------------|--------------|--------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| name        | VARCHAR(100) | NOT NULL                 |
| note        | TEXT         | NULLABLE                 |
| created_at  | DATETIME(6)  | NOT NULL, auto-set       |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set       |

### visit_records

| Column          | Type         | Constraints                          |
|-----------------|--------------|--------------------------------------|
| id              | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| patient_name    | VARCHAR(50)  | NOT NULL                             |
| patient_age     | INT          | NULLABLE                             |
| patient_gender  | VARCHAR(10)  | NULLABLE (MALE, FEMALE)              |
| visit_type      | VARCHAR(15)  | NOT NULL (OUTPATIENT, INPATIENT, EMERGENCY, PHYSICAL_EXAM) |
| visit_date      | DATE         | NOT NULL                             |
| institution_id  | BIGINT       | NOT NULL, FK → medical_institutions(id) |
| medical_content | LONGTEXT     | NULLABLE                             |
| doctor          | VARCHAR(50)  | NULLABLE                             |
| department      | VARCHAR(50)  | NULLABLE                             |
| discharge_date  | DATE         | NULLABLE                             |
| discharge_dept  | VARCHAR(50)  | NULLABLE                             |
| created_at      | DATETIME(6)  | NOT NULL, auto-set                   |
| updated_at      | DATETIME(6)  | NOT NULL, auto-set                   |

### visit_prescriptions

| Column           | Type         | Constraints                          |
|------------------|--------------|--------------------------------------|
| id               | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| visit_id         | BIGINT       | NOT NULL, FK → visit_records(id)     |
| prescription_date| DATE         | NULLABLE                             |
| description      | TEXT         | NULLABLE                             |
| created_at       | DATETIME(6)  | NOT NULL, auto-set                   |
| updated_at       | DATETIME(6)  | NOT NULL, auto-set                   |

### prescription_items

| Column                 | Type         | Constraints                              |
|------------------------|--------------|------------------------------------------|
| id                     | BIGINT       | PRIMARY KEY, AUTO_INCREMENT              |
| prescription_id        | BIGINT       | NOT NULL, FK → visit_prescriptions(id)   |
| medication_reminder_id | BIGINT       | NOT NULL, FK → medication_reminders(id)  |
| note                   | VARCHAR(255) | NULLABLE                                 |
| created_at             | DATETIME(6)  | NOT NULL, auto-set                       |
| updated_at             | DATETIME(6)  | NOT NULL, auto-set                       |

### visit_examinations

| Column      | Type         | Constraints                      |
|-------------|--------------|----------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT      |
| visit_id    | BIGINT       | NOT NULL, FK → visit_records(id) |
| name        | VARCHAR(100) | NOT NULL                         |
| exam_date   | DATE         | NULLABLE                         |
| description | TEXT         | NULLABLE                         |
| created_at  | DATETIME(6)  | NOT NULL, auto-set               |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set               |

### visit_lab_tests

| Column      | Type         | Constraints                      |
|-------------|--------------|----------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT      |
| visit_id    | BIGINT       | NOT NULL, FK → visit_records(id) |
| name        | VARCHAR(100) | NOT NULL                         |
| test_date   | DATE         | NULLABLE                         |
| description | TEXT         | NULLABLE                         |
| created_at  | DATETIME(6)  | NOT NULL, auto-set               |
| updated_at  | DATETIME(6)  | NOT NULL, auto-set               |

### visit_attachments

| Column      | Type         | Constraints                               |
|-------------|--------------|-------------------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT               |
| visit_id    | BIGINT       | NOT NULL, FK → visit_records(id)          |
| file_id     | BIGINT       | NOT NULL, FK → file_records(id)           |
| source_type | VARCHAR(15)  | NOT NULL (PRESCRIPTION, EXAMINATION, LAB_TEST) |
| source_id   | BIGINT       | NOT NULL                                  |
| created_at  | DATETIME(6)  | NOT NULL, auto-set                        |

### visit_invoices

| Column      | Type         | Constraints                               |
|-------------|--------------|-------------------------------------------|
| id          | BIGINT       | PRIMARY KEY, AUTO_INCREMENT               |
| visit_id    | BIGINT       | NOT NULL, FK → visit_records(id)          |
| invoice_id  | BIGINT       | NOT NULL, FK → invoices(id)               |
| source_type | VARCHAR(15)  | NOT NULL (PRESCRIPTION, EXAMINATION, LAB_TEST) |
| source_id   | BIGINT       | NOT NULL                                  |
| created_at  | DATETIME(6)  | NOT NULL, auto-set                        |

### subscriptions

| Column            | Type           | Constraints                             |
|-------------------|----------------|-----------------------------------------|
| id                | BIGINT         | PRIMARY KEY, AUTO_INCREMENT             |
| name              | VARCHAR(200)   | NOT NULL                                |
| description       | TEXT           | NULLABLE                                |
| subscription_type | VARCHAR(20)    | NOT NULL (PERIODIC, ONE_TIME)           |
| billing_mode      | VARCHAR(20)    | NULLABLE (MONTHLY, QUARTERLY, YEARLY)   |
| platform_id       | BIGINT         | NOT NULL, FK → platforms(id)            |
| status            | VARCHAR(20)    | NOT NULL, default ACTIVE (ACTIVE, EXPIRED, CANCELLED) |
| renew_notice_days | INT            | NULLABLE, default 7                     |
| note              | TEXT           | NULLABLE                                |
| created_at        | DATETIME(6)    | NOT NULL, auto-set                      |
| updated_at        | DATETIME(6)    | NOT NULL, auto-set                      |

### subscription_records

| Column            | Type           | Constraints                             |
|-------------------|----------------|-----------------------------------------|
| id                | BIGINT         | PRIMARY KEY, AUTO_INCREMENT             |
| subscription_id   | BIGINT         | NOT NULL, FK → subscriptions(id)        |
| record_date       | DATE           | NOT NULL                                |
| amount            | DECIMAL(12,2)  | NOT NULL                                |
| currency          | VARCHAR(10)    | NULLABLE, default CNY                   |
| start_date        | DATE           | NOT NULL                                |
| end_date          | DATE           | NULLABLE                                |
| quantity          | VARCHAR(100)   | NULLABLE                                |
| order_no          | VARCHAR(100)   | NULLABLE, UNIQUE                        |
| payment_method_id | BIGINT         | NULLABLE, FK → payment_methods(id)      |
| note              | TEXT           | NULLABLE                                |
| expired           | BIT(1)         | NOT NULL, default FALSE                 |
| created_at        | DATETIME(6)    | NOT NULL, auto-set                      |

### subscription_record_attachments

| Column    | Type   | Constraints                                   |
|-----------|--------|-----------------------------------------------|
| id        | BIGINT | PRIMARY KEY, AUTO_INCREMENT                   |
| record_id | BIGINT | NOT NULL, FK → subscription_records(id)       |
| file_id   | BIGINT | NOT NULL, FK → file_records(id)               |

### subscription_record_invoices

| Column     | Type        | Constraints                              |
|------------|-------------|------------------------------------------|
| id         | BIGINT      | PRIMARY KEY, AUTO_INCREMENT              |
| record_id  | BIGINT      | NOT NULL, FK → subscription_records(id)  |
| invoice_id | BIGINT      | NOT NULL, FK → invoices(id)              |
| created_at | DATETIME(6) | NOT NULL, auto-set                       |

UNIQUE INDEX on (record_id, invoice_id).

### payment_methods

| Column       | Type         | Constraints                          |
|--------------|--------------|--------------------------------------|
| id           | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| name         | VARCHAR(100) | UNIQUE, NOT NULL                     |
| logo_file_id | BIGINT       | NULLABLE, FK → file_records(id)      |
| created_at   | DATETIME(6)  | NOT NULL, auto-set                   |
| updated_at   | DATETIME(6)  | NOT NULL, auto-set                   |

### platforms

| Column       | Type         | Constraints                          |
|--------------|--------------|--------------------------------------|
| id           | BIGINT       | PRIMARY KEY, AUTO_INCREMENT          |
| name         | VARCHAR(100) | UNIQUE, NOT NULL                     |
| logo_file_id | BIGINT       | NULLABLE, FK → file_records(id)      |
| website      | VARCHAR(500) | NULLABLE                             |
| created_at   | DATETIME(6)  | NOT NULL, auto-set                   |
| updated_at   | DATETIME(6)  | NOT NULL, auto-set                   |

## Relationships

- `users.role_id` → `roles.id` (Many-to-One: many users can share one role)
- `goods.category_id` → `good_categories.id` (Many-to-One: many goods can share one category)
- `goods.brand_id` → `good_brands.id` (Many-to-One: many goods can share one brand)
- `good_items.good_id` → `goods.id` (Many-to-One: many items belong to one good)
- `good_pictures.good_id` → `goods.id` (Many-to-One: many pictures belong to one good)
- `good_pictures.file_id` → `file_records.id` (Many-to-One: many pictures can reference one file)
- `assets.category_id` → `asset_categories.id` (Many-to-One: many assets share one category)
- `assets.place_id` → `asset_places.id` (Many-to-One: many assets share one place)
- `assets.store_id` → `asset_stores.id` (Many-to-One: many assets can share one store)
- `assets.parent_id` → `assets.id` (Many-to-One: sub-asset references parent)
- `asset_pictures.asset_id` → `assets.id` (Many-to-One: many pictures belong to one asset)
- `asset_pictures.file_id` → `file_records.id` (Many-to-One: many pictures can reference one file)
- `asset_invoices.asset_id` → `assets.id` (Many-to-One: many bindings reference one asset)
- `asset_invoices.invoice_id` → `invoices.id` (Many-to-One: many bindings reference one invoice)
- `invoices.file_id` → `file_records.id` (Many-to-One: invoice references one primary file)
- `invoice_attachments.invoice_id` → `invoices.id` (Many-to-One: many attachments belong to one invoice)
- `invoice_attachments.file_id` → `file_records.id` (Many-to-One: many attachments can reference one file)
- `asset_invoices.asset_id` → `assets.id` (Many-to-One: many bindings reference one asset)
- `asset_invoices.invoice_id` → `invoices.id` (Many-to-One: many bindings reference one invoice)
- `medication_reminders.good_id` → `goods.id` (Many-to-One: many reminders can reference one good)

- `good_attachments.good_id` → `goods.id` (Many-to-One: many attachments belong to one good)
- `good_attachments.file_id` → `file_records.id` (Many-to-One: many attachments can reference one file)

- `asset_attachments.asset_id` → `assets.id` (Many-to-One: many attachments belong to one asset)
- `asset_attachments.file_id` → `file_records.id` (Many-to-One: many attachments can reference one file)

- `visit_records.institution_id` → `medical_institutions.id` (Many-to-One: many visits reference one institution)

- `visit_prescriptions.visit_id` → `visit_records.id` (Many-to-One: many prescriptions belong to one visit)

- `prescription_items.prescription_id` → `visit_prescriptions.id` (Many-to-One: many items belong to one prescription)
- `prescription_items.medication_reminder_id` → `medication_reminders.id` (Many-to-One: many items reference one medication reminder)

- `visit_examinations.visit_id` → `visit_records.id` (Many-to-One: many examinations belong to one visit)

- `visit_lab_tests.visit_id` → `visit_records.id` (Many-to-One: many lab tests belong to one visit)

- `visit_attachments.visit_id` → `visit_records.id` (Many-to-One: many attachments belong to one visit)
- `visit_attachments.file_id` → `file_records.id` (Many-to-One: many attachments can reference one file)

- `visit_invoices.visit_id` → `visit_records.id` (Many-to-One: many bindings reference one visit)
- `visit_invoices.invoice_id` → `invoices.id` (Many-to-One: many bindings reference one invoice)

- `subscriptions.platform_id` → `platforms.id` (Many-to-One: many subscriptions share one platform)

- `subscription_records.subscription_id` → `subscriptions.id` (Many-to-One: many records belong to one subscription)
- `subscription_records.payment_method_id` → `payment_methods.id` (Many-to-One: many records reference one payment method)

- `subscription_record_attachments.record_id` → `subscription_records.id` (Many-to-One: many attachments belong to one record)
- `subscription_record_attachments.file_id` → `file_records.id` (Many-to-One: many attachments can reference one file)

- `subscription_record_invoices.record_id` → `subscription_records.id` (Many-to-One: many bindings reference one record)
- `subscription_record_invoices.invoice_id` → `invoices.id` (Many-to-One: many bindings reference one invoice)

## Initial Data

On first startup, the application seeds system configs including:
- `notification.medication-crontab` = `0 0 7-20 * * ?` (Medication Reminder Check Cron Expression)

On first startup, the application seeds:
- **Roles**: `root` (System administrator role), `member` (Standard member role)
- **Users**: One root user with credentials from environment variables (`ROOT_USERNAME`, `ROOT_PASSWORD`) with `force_change_password = true`
