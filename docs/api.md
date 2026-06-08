# API Documentation

## Base URL

```
http://localhost:8080
```

## Authentication

All endpoints except `/api/auth/login` require a valid JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

---

## Auth Endpoints

### POST /api/auth/login

Authenticate and receive a JWT token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "forceChangePassword": true
}
```

**Response (401):**
```json
{
  "message": "Invalid username or password"
}
```

---

### POST /api/auth/change-password

Change the current user's password. Requires authentication.

**Request Body:**
```json
{
  "currentPassword": "admin123",
  "newPassword": "newSecurePassword123"
}
```

**Response (200):**
```json
{
  "message": "Password changed successfully"
}
```

**Response (400):**
```json
{
  "message": "Validation failed",
  "errors": {
    "newPassword": "size must be between 8 and 2147483647"
  }
}
```

---

## Dashboard Endpoints

All dashboard endpoints require authentication (any role).

### GET /api/dashboard

Get aggregated dashboard data in a single request. Returns stats, expiring items, in-use items, warranty-expiring assets, and in-use assets.

**Response (200):**
```json
{
  "stats": {
    "itemCount": 42,
    "assetCount": 15,
    "totalAssetPrice": 25999.50,
    "invoiceCount": 23,
    "activeSubscriptionCount": 5,
    "monthlySubscriptionSpending": 299.00
  },
  "expiringSoonItems": [
    {
      "id": 5,
      "goodId": 2,
      "productName": "Instant Coffee",
      "categoryName": "Food",
      "brandName": "Nestlé",
      "expirationDate": "2026-05-15",
      "lifeDays": 180,
      "createdAt": "2026-01-01T00:00:00"
    }
  ],
  "inUseItems": [
    {
      "id": 3,
      "goodId": 1,
      "productName": "Milk Powder",
      "categoryName": "Food",
      "brandName": "BrandX",
      "expirationDate": "2026-12-01",
      "lifeDays": 365,
      "createdAt": "2026-04-01T10:00:00"
    }
  ],
  "warrantyExpiringAssets": [
    {
      "id": 1,
      "name": "MacBook Pro",
      "categoryName": "Electronics",
      "placeName": "Office",
      "price": 12999.00,
      "expirationDate": "2026-06-15",
      "shopDate": "2024-06-15"
    }
  ],
  "inUseAssets": [
    {
      "id": 2,
      "name": "Monitor",
      "categoryName": "Electronics",
      "placeName": "Office",
      "price": 2999.00,
      "shopDate": "2026-03-01",
      "hasWarranty": true,
      "warrantyStatus": "IN_WARRANTY",
      "expirationDate": "2027-03-01"
    }
  ],
  "upcomingRenewals": [
    {
      "id": 1,
      "name": "Netflix",
      "platformName": "Netflix",
      "platformLogoUrl": null,
      "endDate": "2026-05-30"
    }
  ]
}
```

**Field descriptions:**

| Field | Description |
|-------|-------------|
| stats.itemCount | Total count of in-use good items |
| stats.assetCount | Total count of all assets |
| stats.totalAssetPrice | Sum of all asset prices |
| stats.invoiceCount | Total count of all invoices |
| stats.activeSubscriptionCount | Total count of active subscriptions |
| stats.monthlySubscriptionSpending | Sum of subscription spending this month |
| expiringSoonItems | Up to 10 items expiring soon (sorted by expirationDate asc) |
| inUseItems | Up to 10 items in use (sorted by createdAt desc) |
| warrantyExpiringAssets | Up to 10 assets with warranty expiring soon (sorted by expirationDate asc) |
| inUseAssets | Up to 10 assets in use (sorted by shopDate desc) |
| upcomingRenewals | Up to 5 subscriptions with records ending within 7 days (sorted by endDate asc) |

---

## Profile Endpoints

All profile endpoints require authentication (any role).

### GET /api/profile

Get the current user's profile.

**Response (200):**
```json
{
  "id": 1,
  "username": "admin",
  "displayName": "Root Administrator",
  "roleName": "root",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

---

### PUT /api/profile

Update the current user's profile. All fields are optional.

**Request Body:**
```json
{
  "displayName": "New Display Name"
}
```

**Response (200):**
```json
{
  "id": 1,
  "username": "admin",
  "displayName": "New Display Name",
  "roleName": "root",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

---

### PUT /api/profile/password

Change the current user's password.

**Request Body:**
```json
{
  "currentPassword": "admin123",
  "newPassword": "newSecurePassword123"
}
```

**Response (200):**
```json
{
  "message": "Password changed successfully"
}
```

**Response (400):**
```json
{
  "message": "Validation failed",
  "errors": {
    "newPassword": "size must be between 8 and 2147483647"
  }
}
```

---

## Member Endpoints

All member endpoints require the `root` role.

### GET /api/members

List all members.

**Response (200):**
```json
[
  {
    "id": 1,
    "username": "admin",
    "displayName": "Root Administrator",
    "roleName": "root",
    "forceChangePassword": false,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### GET /api/members/{id}

Get a specific member by ID.

**Response (200):**
```json
{
  "id": 1,
  "username": "admin",
  "displayName": "Root Administrator",
  "roleName": "root",
  "forceChangePassword": false,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (404):**
```json
{
  "message": "Member not found with id: 99"
}
```

---

### POST /api/members

Create a new member.

**Request Body:**
```json
{
  "username": "john",
  "password": "securePassword123",
  "displayName": "John Doe",
  "roleName": "member"
}
```

**Response (201):**
```json
{
  "id": 2,
  "username": "john",
  "displayName": "John Doe",
  "roleName": "member",
  "forceChangePassword": true,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (409):**
```json
{
  "message": "Username already exists: john"
}
```

---

### PUT /api/members/{id}

Update an existing member. All fields are optional.

**Request Body:**
```json
{
  "displayName": "John D.",
  "roleName": "root",
  "password": "newPassword123"
}
```

**Response (200):**
```json
{
  "id": 2,
  "username": "john",
  "displayName": "John D.",
  "roleName": "root",
  "forceChangePassword": true,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

---

### DELETE /api/members/{id}

Delete a member.

**Response (204):** No content.

**Response (403):**
```json
{
  "message": "Cannot delete the last root user"
}
```

---

## Role Endpoints

All role endpoints require the `root` role.

### GET /api/roles

List all roles.

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "root",
    "description": "System administrator role",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  },
  {
    "id": 2,
    "name": "member",
    "description": "Standard member role",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### GET /api/roles/{id}

Get a specific role by ID.

**Response (200):**
```json
{
  "id": 1,
  "name": "root",
  "description": "System administrator role",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

---

### POST /api/roles

Create a new role.

**Request Body:**
```json
{
  "name": "editor",
  "description": "Content editor role"
}
```

**Response (201):**
```json
{
  "id": 3,
  "name": "editor",
  "description": "Content editor role",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (409):**
```json
{
  "message": "Role already exists: editor"
}
```

---

### PUT /api/roles/{id}

Update an existing role. All fields are optional.

**Request Body:**
```json
{
  "name": "editor",
  "description": "Updated description"
}
```

**Response (200):**
```json
{
  "id": 3,
  "name": "editor",
  "description": "Updated description",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

**Response (403):**
```json
{
  "message": "Cannot rename the root role"
}
```

---

### DELETE /api/roles/{id}

Delete a role.

**Response (204):** No content.

**Response (403):**
```json
{
  "message": "Cannot delete the root role"
}
```

```json
{
  "message": "Cannot delete role that is assigned to users"
}
```

---

## Good Category Endpoints

All good category endpoints require authentication (any role).

### GET /api/good-categories

List all categories.

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Food",
    "description": "Food and beverages",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### GET /api/good-categories/{id}

Get a specific category by ID.

**Response (200):**
```json
{
  "id": 1,
  "name": "Food",
  "description": "Food and beverages",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (404):**
```json
{
  "message": "Category not found with id: 99"
}
```

---

### POST /api/good-categories

Create a new category.

**Request Body:**
```json
{
  "name": "Food",
  "description": "Food and beverages"
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Food",
  "description": "Food and beverages",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (409):**
```json
{
  "message": "Category already exists: Food"
}
```

---

### PUT /api/good-categories/{id}

Update an existing category. All fields are optional.

**Request Body:**
```json
{
  "name": "Food & Drinks",
  "description": "Updated description"
}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "Food & Drinks",
  "description": "Updated description",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

---

### DELETE /api/good-categories/{id}

Delete a category.

**Response (204):** No content.

**Response (403):**
```json
{
  "message": "Cannot delete category that is in use by goods"
}
```

---

## Good Brand Endpoints

All good brand endpoints require authentication (any role).

### GET /api/good-brands

List all brands.

**Response (200):**
```json
[
  {
    "id": 1,
    "brandName": "Nestlé",
    "companyName": "Nestlé S.A.",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### GET /api/good-brands/{id}

Get a specific brand by ID.

**Response (200):**
```json
{
  "id": 1,
  "brandName": "Nestlé",
  "companyName": "Nestlé S.A.",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (404):**
```json
{
  "message": "Brand not found with id: 99"
}
```

---

### POST /api/good-brands

Create a new brand.

**Request Body:**
```json
{
  "brandName": "Nestlé",
  "companyName": "Nestlé S.A."
}
```

**Response (201):**
```json
{
  "id": 1,
  "brandName": "Nestlé",
  "companyName": "Nestlé S.A.",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (409):**
```json
{
  "message": "Brand already exists: Nestlé"
}
```

---

### PUT /api/good-brands/{id}

Update an existing brand. All fields are optional.

**Request Body:**
```json
{
  "brandName": "Nestlé Updated",
  "companyName": "Nestlé Group"
}
```

**Response (200):**
```json
{
  "id": 1,
  "brandName": "Nestlé Updated",
  "companyName": "Nestlé Group",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

---

### DELETE /api/good-brands/{id}

Delete a brand.

**Response (204):** No content.

**Response (403):**
```json
{
  "message": "Cannot delete brand that is in use by goods"
}
```

---

## Good Endpoints

All good endpoints require authentication (any role).

### GET /api/goods

List goods with server-side pagination, search, and filtering.

**Query Parameters:**

| Parameter  | Type    | Default     | Description                                      |
|------------|---------|-------------|--------------------------------------------------|
| search     | String  | null        | Search by product name or barcode (partial match) |
| categoryId | Long    | null        | Filter by category ID                            |
| brandId    | Long    | null        | Filter by brand ID                               |
| status     | String  | null        | Filter by good status: IN_USE, NOT_IN_USE        |
| itemStatus | String  | null        | Filter by item status: EXPIRED, EXPIRING_SOON, IN_USE, EXHAUSTED |
| page       | int     | 0           | Page number (0-indexed)                          |
| size       | int     | 10          | Page size                                        |
| sortBy     | String  | createdAt   | Sort field                                       |
| sortDir    | String  | desc        | Sort direction: asc or desc                      |

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "productName": "Instant Coffee",
      "barcode": "1234567890123",
      "categoryName": "Food",
      "categoryId": 1,
      "brandName": "Nestlé",
      "brandId": 1,
      "expiringSoonDays": 30,
      "itemCountTotal": 3,
      "itemCountInUse": 2,
      "status": "IN_USE",
      "firstPictureUrl": "/api/goods/1/pictures/1/file",
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

---

### GET /api/goods/{id}

Get a specific good by ID with full details (items and pictures).

**Response (200):**
```json
{
  "id": 1,
  "productName": "Instant Coffee",
  "barcode": "1234567890123",
  "categoryName": "Food",
  "categoryId": 1,
  "brandName": "Nestlé",
  "brandId": 1,
  "expiringSoonDays": 30,
  "itemCountTotal": 3,
  "itemCountInUse": 2,
  "status": "IN_USE",
  "firstPictureUrl": "/api/goods/1/pictures/1/file",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00",
  "items": [
    {
      "id": 1,
      "productDate": "2026-01-01",
      "expirationDate": "2026-07-01",
      "lifeDays": 181,
      "inUse": true,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ],
  "pictures": [
    {
      "id": 1,
      "filename": "coffee.jpg",
      "contentType": "image/jpeg",
      "fileSize": 102400,
      "url": "/api/goods/1/pictures/1/file",
      "createdAt": "2026-01-01T00:00:00"
    }
  ]
}
```

**Response (404):**
```json
{
  "message": "Good not found with id: 99"
}
```

---

### GET /api/goods/barcode/{barcode}

Look up a good by barcode.

**Response (200):**
```json
{
  "id": 1,
  "productName": "Instant Coffee",
  "barcode": "1234567890123",
  "categoryName": "Food",
  "categoryId": 1,
  "brandName": "Nestlé",
  "brandId": 1,
  "expiringSoonDays": 30,
  "itemCountTotal": 3,
  "itemCountInUse": 2,
  "status": "IN_USE",
  "firstPictureUrl": "/api/goods/1/pictures/1/file",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
```

**Response (404):**
```json
{
  "message": "Good not found with barcode: 9999999999999"
}
```

---

### POST /api/goods

Create a new good.

**Request Body:**
```json
{
  "productName": "Instant Coffee",
  "barcode": "1234567890123",
  "categoryId": 1,
  "brandId": 1,
  "expiringSoonDays": 30
}
```

- `expiringSoonDays` is optional (defaults to 30)

**Response (201):**
```json
{
  "id": 1,
  "productName": "Instant Coffee",
  "barcode": "1234567890123",
  "categoryName": "Food",
  "categoryId": 1,
  "brandName": "Nestlé",
  "brandId": 1,
  "expiringSoonDays": 30,
  "itemCountTotal": 0,
  "itemCountInUse": 0,
  "status": "EXHAUSTED",
  "firstPictureUrl": null,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00",
  "items": [],
  "pictures": []
}
```

**Response (409):**
```json
{
  "message": "Barcode already exists: 1234567890123"
}
```

---

### PUT /api/goods/{id}

Update an existing good. All fields are optional.

**Request Body:**
```json
{
  "productName": "Premium Coffee",
  "barcode": "1234567890124",
  "categoryId": 2,
  "brandId": 2,
  "expiringSoonDays": 14
}
```

**Response (200):**
```json
{
  "id": 1,
  "productName": "Premium Coffee",
  "barcode": "1234567890124",
  "categoryName": "Beverages",
  "categoryId": 2,
  "brandName": "Starbucks",
  "brandId": 2,
  "expiringSoonDays": 14,
  "itemCountTotal": 3,
  "itemCountInUse": 2,
  "status": "IN_USE",
  "firstPictureUrl": "/api/goods/1/pictures/1/file",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00",
  "items": [...],
  "pictures": [...]
}
```

---

### DELETE /api/goods/{id}

Delete a good. Goods with items cannot be deleted — delete all items first.

**Response (204):** No content.

**Response (403):**
```json
{
  "message": "Cannot delete good that has items. Delete all items first."
}
```

---

## Good Item Endpoints

All good item endpoints require authentication (any role). Items are nested under goods.

### GET /api/goods/{goodId}/items

List all items for a good.

**Response (200):**
```json
[
  {
    "id": 1,
    "productDate": "2026-01-01",
    "expirationDate": "2026-07-01",
    "lifeDays": 181,
    "inUse": true,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### POST /api/goods/{goodId}/items

Create items. Provide exactly 2 of the 3 date fields (`productDate`, `expirationDate`, `lifeDays`) — the third is auto-calculated. Use `quantity` to create multiple items with the same dates at once.

**Request Body (example with productDate + lifeDays):**
```json
{
  "productDate": "2026-01-01",
  "lifeDays": 181,
  "inUse": true,
  "quantity": 3
}
```

**Request Body (example with productDate + expirationDate):**
```json
{
  "productDate": "2026-01-01",
  "expirationDate": "2026-07-01"
}
```

- `inUse` is optional (defaults to true)
- `quantity` is optional (defaults to 1)

**Response (201):**
```json
[
  {
    "id": 1,
    "productDate": "2026-01-01",
    "expirationDate": "2026-07-01",
    "lifeDays": 181,
    "inUse": true,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

**Response (400):**
```json
{
  "message": "Exactly 2 of the 3 date fields (productDate, expirationDate, lifeDays) must be provided"
}
```

---

### PUT /api/goods/{goodId}/items/{itemId}

Update an existing item. All fields are optional. If any 2 date fields are provided, the third is recalculated.

**Request Body:**
```json
{
  "productDate": "2026-02-01",
  "expirationDate": "2026-08-01",
  "inUse": false
}
```

**Response (200):**
```json
{
  "id": 1,
  "productDate": "2026-02-01",
  "expirationDate": "2026-08-01",
  "lifeDays": 181,
  "inUse": false,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-02T00:00:00"
}
```

---

### DELETE /api/goods/{goodId}/items/{itemId}

Delete an item.

**Response (204):** No content.

---

## File Endpoints

All file endpoints require authentication (any role). Supports any file type. Max file size: 100MB.

### GET /api/files

List files with server-side pagination and optional filtering.

**Query Parameters:**

| Parameter   | Type   | Default | Description                                                                 |
|-------------|--------|---------|-----------------------------------------------------------------------------|
| search      | string | null    | Filter by filename (partial match)                                          |
| contentType | string | null    | Filter by content type prefix (e.g. `image/`, `video/`, `application/pdf`) |
| status      | string | null    | Filter by processing status: `SUCCESS`, `FAILED`, `PROCESSING`             |
| page        | int    | 0       | Page number                                                                 |
| size        | int    | 20      | Page size                                                                   |

**Response (200):** Spring `Page<FileResponse>`

---

### POST /api/files

Upload a file. Uses multipart/form-data.

**Request:** `multipart/form-data` with field `file`

**Response (201):**
```json
{
  "id": 1,
  "originalFilename": "document.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "url": "/api/files/1/download",
  "createdAt": "2026-01-01T00:00:00",
  "indexed": true,
  "extractStatus": "PENDING",
  "chunkStatus": "PENDING"
}
```

---

### GET /api/files/{id}

Get file metadata by ID.

**Response (200):**
```json
{
  "id": 1,
  "originalFilename": "document.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "url": "/api/files/1/download",
  "createdAt": "2026-01-01T00:00:00",
  "indexed": true,
  "extractStatus": "SUCCESS",
  "chunkStatus": "SUCCESS"
}
```

**Response (404):**
```json
{
  "message": "File not found with id: 99"
}
```

---

### GET /api/files/{id}/preview

Preview a file inline. Returns the raw file bytes with `Content-Disposition: inline` header.

**Response (200):** Binary file data with `Content-Type` header.

---

### GET /api/files/{id}/download

Download a file. Returns the raw file bytes with `Content-Disposition: attachment` header using the original filename.

**Response (200):** Binary file data with `Content-Type` and `Content-Disposition` headers.

---

### PATCH /api/files/{id}/rename

Rename a file's original filename.

**Request Body:**
```json
{
  "originalFilename": "new-name.pdf"
}
```

**Response (200):** `FileResponse` with updated filename.

---

### DELETE /api/files/{id}

Delete a file (removes from disk and database).

**Response (204):** No content.

---

### POST /api/files/{id}/retry

Retry failed text extraction or chunking/indexing for a file. Only re-executes the failed step(s).

**Response (202):** Accepted (processing is asynchronous).

---

## Good Picture Endpoints

All good picture endpoints require authentication (any role). Pictures are nested under goods. Max file size: 100MB.

### POST /api/goods/{goodId}/pictures

Upload a picture for a good. Uses multipart/form-data.

**Request:** `multipart/form-data` with field `file`

**Response (201):**
```json
{
  "id": 1,
  "filename": "coffee.jpg",
  "contentType": "image/jpeg",
  "fileSize": 102400,
  "url": "/api/goods/1/pictures/1/file",
  "createdAt": "2026-01-01T00:00:00"
}
```

---

### GET /api/goods/{goodId}/pictures/{pictureId}/file

Download/serve a picture file. Returns the raw image bytes with the appropriate content type header.

**Response (200):** Binary image data with `Content-Type` header (e.g., `image/jpeg`).

---

### DELETE /api/goods/{goodId}/pictures/{pictureId}

Delete a picture (removes from disk and database).

**Response (204):** No content.

---

## Good Attachment Endpoints

All good attachment endpoints require authentication (any role). Attachments are nested under goods.

### GET /api/goods/{goodId}/attachments

List all attachments for a good.

**Response (200):**
```json
[
  {
    "id": 1,
    "filename": "manual.pdf",
    "contentType": "application/pdf",
    "fileSize": 204800,
    "url": "/api/goods/1/attachments/1/file",
    "createdAt": "2026-01-01T00:00:00"
  }
]
```

---

### POST /api/goods/{goodId}/attachments

Upload an attachment for a good. Uses multipart/form-data. Max file size: 100MB.

**Request:** `multipart/form-data` with field `file`

**Response (201):** `GoodAttachmentResponse`

---

### GET /api/goods/{goodId}/attachments/{attachmentId}/file

Download/serve an attachment file.

**Response (200):** Binary file data with appropriate `Content-Type`.

---

### DELETE /api/goods/{goodId}/attachments/{attachmentId}

Delete an attachment.

**Response (204):** No content.

---

## Asset Category Endpoints

### GET /api/asset-categories

List all asset categories.

**Response (200):** `AssetCategoryResponse[]`

### GET /api/asset-categories/{id}

Get asset category by ID.

### POST /api/asset-categories

Create a new asset category.

**Request Body:**
```json
{ "name": "Electronics", "description": "Electronic devices" }
```

**Response (201):** `AssetCategoryResponse`

### PUT /api/asset-categories/{id}

Update an asset category. All fields optional.

### DELETE /api/asset-categories/{id}

Delete an asset category. Cannot delete if used by assets.

---

## Asset Place Endpoints

### GET /api/asset-places

List all asset places.

**Response (200):** `AssetPlaceResponse[]`

### GET /api/asset-places/{id}

Get asset place by ID.

### POST /api/asset-places

Create a new asset place.

**Request Body:**
```json
{ "name": "Living Room", "description": "Main living area" }
```

**Response (201):** `AssetPlaceResponse`

### PUT /api/asset-places/{id}

Update an asset place. All fields optional.

### DELETE /api/asset-places/{id}

Delete an asset place. Cannot delete if used by assets.

---

## Asset Store Endpoints

### GET /api/asset-stores

List all asset stores.

**Response (200):** `AssetStoreResponse[]`

### GET /api/asset-stores/{id}

Get asset store by ID.

### POST /api/asset-stores

Create a new asset store.

**Request Body:**
```json
{ "name": "JD.com", "channel": "Online" }
```

**Response (201):** `AssetStoreResponse`

### PUT /api/asset-stores/{id}

Update an asset store. All fields optional.

### DELETE /api/asset-stores/{id}

Delete an asset store. Cannot delete if used by assets.

---

## Asset Endpoints

### GET /api/assets

List assets with server-side pagination, search, and filtering.

**Query Parameters:**

| Parameter       | Type   | Default     | Description                           |
|-----------------|--------|-------------|---------------------------------------|
| search          | string | —           | Filter by name, barcode, or serial    |
| categoryId      | long   | —           | Filter by category ID                 |
| placeId         | long   | —           | Filter by place ID                    |
| isInUse         | bool   | —           | Filter by in-use status               |
| warrantyStatus  | enum   | —           | IN_WARRANTY, OUT_WARRANTY, NO_WARRANTY |
| parentOnly      | bool   | —           | Filter top-level assets only (parent_id IS NULL) |
| page            | int    | 0           | Page number                           |
| size            | int    | 10          | Page size                             |
| sortBy          | string | createdAt   | Sort field                            |
| sortDir         | string | desc        | Sort direction (asc/desc)             |

**Response (200):** Spring `Page<AssetResponse>`

### GET /api/assets/{id}

Get asset by ID with sub-assets and pictures.

**Response (200):** `AssetDetailResponse`

### POST /api/assets

Create a new asset.

**Request Body:**
```json
{
  "name": "MacBook Pro",
  "barcode": "123456789",
  "serialNumber": "C02X12345",
  "categoryId": 1,
  "placeId": 1,
  "inUse": true,
  "retireDate": "2025-12-01",
  "price": 1299.99,
  "shopDate": "2024-01-15",
  "storeId": 1,
  "hasWarranty": true,
  "activeDate": "2024-01-15",
  "warrantyPeriod": 365,
  "note": "Work laptop"
}
```

Warranty dates follow "fill 2 of 3" logic: provide exactly 2 of (activeDate, warrantyPeriod, expirationDate) and the third is auto-calculated.

Retire date: when `inUse` is `false`, `retireDate` is required and must not be in the future. When `inUse` is `true`, `retireDate` is cleared automatically.

**Response (201):** `AssetDetailResponse`

### PUT /api/assets/{id}

Update an asset. All fields optional.

### DELETE /api/assets/{id}

Delete an asset. Cannot delete if it has sub-assets.

---

## Asset Picture Endpoints

### POST /api/assets/{assetId}/pictures

Upload a picture (multipart/form-data, max 100MB).

### GET /api/assets/{assetId}/pictures/{pictureId}/file

Serve picture inline with appropriate content type.

**Response (200):** Binary image data.

### DELETE /api/assets/{assetId}/pictures/{pictureId}

Delete a picture.

**Response (204):** No content.

---

## Asset Attachment Endpoints

All asset attachment endpoints require authentication (any role). Attachments are nested under assets.

### GET /api/assets/{assetId}/attachments

List all attachments for an asset.

**Response (200):** `AssetAttachmentResponse[]`

```json
[
  {
    "id": 1,
    "filename": "manual.pdf",
    "contentType": "application/pdf",
    "fileSize": 204800,
    "url": "/api/assets/1/attachments/1/file",
    "createdAt": "2026-01-01T00:00:00"
  }
]
```

### POST /api/assets/{assetId}/attachments

Upload an attachment for an asset. Uses multipart/form-data. Max file size: 100MB.

**Request:** `multipart/form-data` with field `file`

**Response (201):** `AssetAttachmentResponse`

### GET /api/assets/{assetId}/attachments/{attachmentId}/file

Download/serve an attachment file.

**Response (200):** Binary file data with appropriate `Content-Type`.

### DELETE /api/assets/{assetId}/attachments/{attachmentId}

Delete an attachment.

**Response (204):** No content.

---

## Asset Invoice Binding Endpoints

### GET /api/assets/{id}/invoices

List all invoices bound to an asset.

**Response (200):** `AssetInvoiceResponse[]`
```json
[
  {
    "id": 1,
    "invoiceId": 5,
    "invoiceNumber": "12345678",
    "invoiceDate": "2024-01-15",
    "invoiceType": "DIGITAL_INVOICE",
    "invoiceStatus": "NORMAL",
    "totalAmount": 113.00
  }
]
```

### POST /api/assets/{assetId}/invoices/{invoiceId}

Bind an invoice to an asset. Creates a many-to-many relationship.

**Response (204):** No content.

### DELETE /api/assets/{assetId}/invoices/{invoiceId}

Unbind an invoice from an asset. Does not delete the invoice.

**Response (204):** No content.

---

## Invoice Endpoints

All invoice endpoints require authentication (any role).

### GET /api/invoices

List invoices with server-side pagination, search, and filtering.

**Query Parameters:**

| Parameter     | Type   | Default   | Description                           |
|---------------|--------|-----------|---------------------------------------|
| search        | string | —         | Filter by invoiceNumber, buyerName, sellerName |
| invoiceType   | enum   | —         | DIGITAL_INVOICE, RAILWAY_ELECTRONIC, VAT_INVOICE, AIR_ELECTRONIC, GENERAL_MACHINE_PRINTED, QUOTA_INVOICE, NON_TAX_INCOME_GENERAL, NON_TAX_INCOME_UNIFIED, FUND_SETTLEMENT, MEDICAL_OUTPATIENT, MEDICAL_INPATIENT, OTHER |
| invoiceStatus | enum   | —         | NORMAL, VOIDED, RED_FLUSHED           |
| buyerName     | string | —         | Exact match on buyer name             |
| sellerName    | string | —         | Exact match on seller name            |
| page          | int    | 0         | Page number                           |
| size          | int    | 10        | Page size                             |
| sortBy        | string | createdAt | Sort field                            |
| sortDir       | string | desc      | Sort direction (asc/desc)             |

**Response (200):** Spring `Page<InvoiceResponse>`

```json
{
  "content": [
    {
      "id": 1,
      "invoiceNumber": "12345678",
      "invoiceDate": "2026-01-15",
      "invoiceType": "DIGITAL_INVOICE",
      "invoiceStatus": "NORMAL",
      "sellerName": "某公司",
      "buyerName": "某购买方",
      "amount": 100.00,
      "taxAmount": 13.00,
      "totalAmount": 113.00,
      "attachmentCount": 0,
      "createdAt": "2026-01-15T10:30:00",
      "updatedAt": "2026-01-15T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### GET /api/invoices/{id}

Get invoice by ID with full details and attachments.

**Response (200):** `InvoiceDetailResponse`

### POST /api/invoices

Create a new invoice (manual input).

**Request Body:**
```json
{
  "invoiceType": "DIGITAL_INVOICE",
  "totalAmount": 113.00,
  "invoiceNumber": "12345678",
  "invoiceDate": "2026-01-15",
  "buyerName": "某购买方",
  "sellerName": "某公司",
  "amount": 100.00,
  "taxAmount": 13.00,
  "fileId": 42,
  "previewImage": "iVBORw0KGgo..."
}
```

Only `invoiceType` and `totalAmount` are required. All other fields are optional. `invoiceNumber` must be unique if provided. `fileId` links the uploaded invoice file. `previewImage` stores the base64-encoded PNG preview of the invoice file.

**Response (201):** `InvoiceDetailResponse`

**Error (409):** Returned if `invoiceNumber` already exists.
```json
{ "message": "Invoice number already exists: 12345678" }
```

### POST /api/invoices/parse

Upload an invoice file (PDF, XML, OFD) and extract invoice data. Returns parsed data for review — does NOT save the invoice.

**Request:** `multipart/form-data` with field `file`

**Response (200):**
```json
{
  "invoiceNumber": "12345678",
  "invoiceDate": "2026-01-15",
  "invoiceType": "DIGITAL_INVOICE",
  "invoiceStatus": "NORMAL",
  "buyerName": "某购买方",
  "sellerName": "某公司",
  "amount": 100.00,
  "taxAmount": 13.00,
  "totalAmount": 113.00,
  "fileId": 42,
  "previewImage": "iVBORw0KGgo..."
}
```

All fields may be null if parsing fails to extract them. `fileId` is the ID of the uploaded file record. `previewImage` is the base64-encoded PNG image of the first page (for PDF/OFD files), which should be passed back in the create request to persist it.

### PUT /api/invoices/{id}

Update an invoice. All fields optional. If `invoiceNumber` is provided, it must be unique (excluding the current invoice).

**Error (409):** Returned if `invoiceNumber` already exists on another invoice.
```json
{ "message": "Invoice number already exists: 12345678" }
```

### DELETE /api/invoices/{id}

Delete an invoice. Cascades to delete all attachments and associated files.

**Response (204):** No content.

### GET /api/invoices/{id}/file/preview

Preview the primary invoice file inline.

**Response (200):** Binary file data with `Content-Disposition: inline`.

### GET /api/invoices/{id}/file/download

Download the primary invoice file.

**Response (200):** Binary file data with `Content-Disposition: attachment`.

### POST /api/invoices/{invoiceId}/attachments

Upload an attachment file. Uses multipart/form-data.

**Request:** `multipart/form-data` with field `file`

**Response (201):**
```json
{
  "id": 1,
  "filename": "receipt.jpg",
  "contentType": "image/jpeg",
  "fileSize": 102400,
  "url": "/api/invoices/1/attachments/1/file",
  "createdAt": "2026-01-15T10:30:00"
}
```

### DELETE /api/invoices/{invoiceId}/attachments/{attachmentId}

Delete an attachment.

**Response (204):** No content.

### GET /api/invoices/{invoiceId}/attachments/{attachmentId}/file

Download/serve an attachment file.

**Response (200):** Binary file data with appropriate `Content-Type`.

---

## System Config Endpoints

All system config endpoints require the `root` role.

### GET /api/system-config

Get system config values for a group.

**Query Parameters:**

| Parameter | Type   | Default | Description                            |
|-----------|--------|---------|----------------------------------------|
| group     | string | —       | Config group: `qiniu`, `ai`, `notification`, `elasticsearch` (enabled toggle only) |

**Response (200):**
```json
{
  "group": "qiniu",
  "items": [
    {
      "key": "qiniu.access-key",
      "value": "ak****na",
      "sensitive": true,
      "description": "Qiniu Access Key"
    }
  ]
}
```

Sensitive values are masked with `****` in the response.

---

### PUT /api/system-config/{group}

Update system config values for a group. Sensitive values passed as `****` (masked) are skipped (not updated).

**Request Body:**
```json
{
  "qiniu.access-key": "your_new_access_key",
  "qiniu.bucket": "new-bucket"
}
```

**Response (200):** No content.

Qiniu group changes trigger a hot-reload of the file storage strategy.
Elasticsearch group changes trigger a hot-reload of the ES client connection.

---

### POST /api/system-config/test/qiniu

Test the Qiniu OSS connection using current config values.

**Response (200):**
```json
{
  "success": true,
  "message": "Qiniu connection successful"
}
```

---

### POST /api/system-config/test/ai

Test the AI model connection using the currently active AI model config.

**Response (200):**
```json
{
  "success": true,
  "message": "AI connection successful"
}
```

---

### POST /api/system-config/test/webhook

Test the webhook configuration by sending a test payload to the configured URL.

**Response (200):**
```json
{
  "success": true,
  "message": "Webhook test request sent"
}
```

---

### POST /api/system-config/test/elasticsearch

Test the Elasticsearch connection using current config values.

**Response (200):**
```json
{
  "success": true,
  "message": "Elasticsearch connection successful"
}
```

---

---

## Search Endpoints

All search endpoints require authentication (any role).

### GET /api/search

Search file contents via Elasticsearch. Returns paginated results grouped by file, with highlighted snippets.

**Query Parameters:**

| Parameter | Type   | Default | Description      |
|-----------|--------|---------|------------------|
| q         | string | —       | Search query     |
| page      | int    | 0       | Page number      |
| size      | int    | 20      | Results per page |

**Response (200):**

```json
{
  "content": [
    {
      "fileId": 1,
      "originalFilename": "manual.pdf",
      "contentType": "application/pdf",
      "fileSize": 1024000,
      "sources": [{ "type": "ASSET", "typeLabel": "资产", "sourceId": 5, "sourceName": "Laptop" }],
      "matches": [{ "chunkId": 42, "page": 3, "snippet": "the <mark>warranty</mark> covers...", "matchTerms": ["warranty"] }],
      "score": 2.5
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

Returns empty results when Elasticsearch is not configured or unavailable.

---

### GET /api/search/status

Check whether Elasticsearch search is available.

**Response (200):**

```json
{
  "available": true
}
```

---

## Notification Endpoints

All notification endpoints require authentication (any role).

### GET /api/notifications

List notifications with pagination, ordered by creation time descending.

**Query Parameters:**

| Parameter | Type    | Default | Description       |
|-----------|---------|---------|-------------------|
| page      | integer | 0       | Page number       |
| size      | integer | 20      | Items per page    |

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "type": "ITEM_EXPIRING",
      "title": "物品即将过期",
      "content": "【牛奶】将于 2026-06-01 过期（剩余 15 天），品牌：蒙牛，分类：食品",
      "isRead": false,
      "createdAt": "2026-05-17T03:00:00",
      "readAt": null
    }
  ],
  "totalPages": 1,
  "totalElements": 1,
  "number": 0,
  "size": 20
}
```

---

### GET /api/notifications/unread-count

Get the count of unread notifications.

**Response (200):**
```
5
```

---

### PUT /api/notifications/{id}/read

Mark a single notification as read.

**Response (200):** No content.

---

### PUT /api/notifications/read-all

Mark all unread notifications as read.

**Response (200):** No content.

---

### POST /api/notifications/test-webhook

Test the webhook by sending a test notification to the configured webhook URL.

**Response (200):**
```json
{
  "success": true,
  "message": "Webhook test request sent"
}
```

---

## Medication Reminder Endpoints

All medication reminder endpoints require authentication (any role).

### GET /api/medications

List medication reminders with pagination, ordered by creation time descending.

**Query Parameters:**

| Parameter | Type    | Default | Description              |
|-----------|---------|---------|--------------------------|
| page      | integer | 0       | Page number              |
| size      | integer | 10      | Items per page           |
| enabled   | boolean | —       | Filter by enabled status |

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "goodId": 5,
      "productName": "阿莫西林",
      "categoryName": "药品",
      "brandName": "白云山",
      "dosageMethod": "口服",
      "dosageQuantity": "2",
      "dosageUnit": "粒",
      "dosageNote": "饭后服用",
      "frequencyHours": "8,12,18",
      "courseStartDate": "2026-05-01",
      "courseEndDate": "2026-05-14",
      "enabled": true,
      "createdAt": "2026-05-17T10:00:00",
      "updatedAt": "2026-05-17T10:00:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 1,
  "number": 0,
  "size": 10
}
```

---

### GET /api/medications/{id}

Get a specific medication reminder by ID.

**Response (200):** `MedicationReminderResponse` (same structure as above)

**Response (404):**
```json
{ "message": "Medication reminder not found with id: 99" }
```

---

### POST /api/medications

Create a new medication reminder.

**Request Body:**
```json
{
  "goodId": 5,
  "dosageMethod": "口服",
  "dosageQuantity": "2",
  "dosageUnit": "粒",
  "dosageNote": "饭后服用",
  "frequencyHours": "8,12,18",
  "courseStartDate": "2026-05-01",
  "courseEndDate": "2026-05-14",
  "enabled": true
}
```

- `enabled` is optional (defaults to true)

**Response (201):** `MedicationReminderResponse`

**Response (409):**
```json
{ "message": "A medication reminder already exists for this good and course period" }
```

---

### PUT /api/medications/{id}

Update an existing medication reminder. All fields are optional.

**Request Body:**
```json
{
  "enabled": false
}
```

**Response (200):** `MedicationReminderResponse`

---

### DELETE /api/medications/{id}

Delete a medication reminder.

**Response (204):** No content.

---

## Medical Institution Endpoints

All medical institution endpoints require authentication (any role).

### GET /api/medical-institutions

List all medical institutions.

**Response (200):** `MedicalInstitutionResponse[]`

```json
[
  {
    "id": 1,
    "name": "市人民医院",
    "address": "人民路100号",
    "phone": "010-12345678",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

### GET /api/medical-institutions/page

List medical institutions with pagination and optional name filter.

**Query Parameters:**

| Parameter | Type | Default | Description        |
|-----------|------|---------|-------------------|
| page      | int  | 0       | Page number        |
| size      | int  | 20      | Page size          |
| name      | string | —     | Filter by name     |

**Response (200):** `Page<MedicalInstitutionResponse>`

### GET /api/medical-institutions/{id}

Get a specific medical institution by ID.

### POST /api/medical-institutions

Create a new medical institution.

**Request Body:**
```json
{
  "name": "市人民医院",
  "address": "人民路100号",
  "phone": "010-12345678"
}
```

**Response (201):** `MedicalInstitutionResponse`

### PUT /api/medical-institutions/{id}

Update a medical institution. All fields are optional.

### DELETE /api/medical-institutions/{id}

Delete a medical institution. Cannot delete if referenced by visit records.

**Response (204):** No content.

---

## Visit Record Endpoints

All visit record endpoints require authentication (any role).

### GET /api/visit-records

List visit records with server-side pagination and filtering.

**Query Parameters:**

| Parameter     | Type   | Default | Description              |
|---------------|--------|---------|--------------------------|
| page          | int    | 0       | Page number              |
| size          | int    | 10      | Page size                |
| visitType     | enum   | —       | OUTPATIENT, INPATIENT, EMERGENCY, PHYSICAL_EXAM |
| startDate     | date   | —       | Filter visits on or after this date |
| endDate       | date   | —       | Filter visits on or before this date |
| institutionId | long   | —       | Filter by medical institution ID |
| patientName   | string | —       | Filter by patient name   |

**Response (200):** `Page<VisitRecordResponse>`

### GET /api/visit-records/patient-names

Get distinct patient names for autocomplete suggestions.

**Response (200):** `["张三", "李四"]`

### GET /api/visit-records/{id}

Get a visit record by ID with full details.

**Response (200):** `VisitRecordResponse`

### POST /api/visit-records

Create a new visit record.

**Request Body:**
```json
{
  "visitType": "OUTPATIENT",
  "visitDate": "2026-05-15",
  "institutionId": 1,
  "department": "内科",
  "doctorName": "王医生",
  "patientName": "张三",
  "patientGender": "MALE",
  "patientAge": 35,
  "chiefComplaint": "头痛三天",
  "diagnosis": "偏头痛",
  "note": "注意休息"
}
```

**Response (201):** `VisitRecordResponse`

### POST /api/visit-records/parse

Parse unstructured text (via AI) to extract visit record data. Returns parsed data for review — does NOT save.

**Request Body:**
```json
{
  "text": "2026年5月15日，去市人民医院看内科..."
}
```

**Response (200):** `VisitRecordParseResponse`

### PUT /api/visit-records/{id}

Update an existing visit record. All fields optional.

### DELETE /api/visit-records/{id}

Delete a visit record. Cascades to delete all sub-records.

**Response (204):** No content.

---

## Visit Prescription Endpoints

All visit prescription endpoints require authentication (any role). Prescriptions are nested under visit records.

### GET /api/visit-records/{visitId}/prescriptions

List prescriptions for a visit record with pagination.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page      | int  | 0       | Page number |
| size      | int  | 10      | Page size   |

**Response (200):** `Page<VisitPrescriptionResponse>`

### GET /api/visit-records/{visitId}/prescriptions/{id}

Get a specific prescription by ID.

### POST /api/visit-records/{visitId}/prescriptions

Create a new prescription.

**Request Body:**
```json
{
  "prescriptionDate": "2026-05-15",
  "doctorName": "王医生",
  "note": "按说明服用"
}
```

**Response (201):** `VisitPrescriptionResponse`

### PUT /api/visit-records/{visitId}/prescriptions/{id}

Update a prescription. All fields optional.

### DELETE /api/visit-records/{visitId}/prescriptions/{id}

Delete a prescription. Cascades to delete all items.

**Response (204):** No content.

---

## Prescription Item Endpoints

All prescription item endpoints require authentication (any role). Items are nested under prescriptions.

### POST /api/visit-records/{visitId}/prescriptions/{prescriptionId}/items

Add an item to a prescription.

**Request Body:**
```json
{
  "drugName": "布洛芬",
  "specification": "0.3g×24片",
  "dosage": "1片",
  "frequency": "每日2次",
  "quantity": 24,
  "unit": "片",
  "note": "饭后服用"
}
```

**Response (201):** `PrescriptionItemResponse`

### PUT /api/visit-records/{visitId}/prescriptions/{prescriptionId}/items/{itemId}

Update a prescription item. All fields optional.

### DELETE /api/visit-records/{visitId}/prescriptions/{prescriptionId}/items/{itemId}

Delete a prescription item.

**Response (204):** No content.

---

## Visit Examination Endpoints

All visit examination endpoints require authentication (any role). Examinations are nested under visit records.

### GET /api/visit-records/{visitId}/examinations

List examinations for a visit record with pagination.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page      | int  | 0       | Page number |
| size      | int  | 10      | Page size   |

**Response (200):** `Page<VisitExaminationResponse>`

### GET /api/visit-records/{visitId}/examinations/{id}

Get a specific examination by ID.

### POST /api/visit-records/{visitId}/examinations

Create a new examination record.

**Request Body:**
```json
{
  "examDate": "2026-05-15",
  "examName": "血常规",
  "examResult": "白细胞偏高",
  "note": ""
}
```

**Response (201):** `VisitExaminationResponse`

### PUT /api/visit-records/{visitId}/examinations/{id}

Update an examination. All fields optional.

### DELETE /api/visit-records/{visitId}/examinations/{id}

Delete an examination.

**Response (204):** No content.

---

## Visit Lab Test Endpoints

All visit lab test endpoints require authentication (any role). Lab tests are nested under visit records.

### GET /api/visit-records/{visitId}/lab-tests

List lab tests for a visit record with pagination.

**Query Parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| page      | int  | 0       | Page number |
| size      | int  | 10      | Page size   |

**Response (200):** `Page<VisitLabTestResponse>`

### GET /api/visit-records/{visitId}/lab-tests/{id}

Get a specific lab test by ID.

### POST /api/visit-records/{visitId}/lab-tests

Create a new lab test record.

**Request Body:**
```json
{
  "testDate": "2026-05-15",
  "testName": "肝功能",
  "testResult": "ALT: 45 U/L",
  "note": ""
}
```

**Response (201):** `VisitLabTestResponse`

### PUT /api/visit-records/{visitId}/lab-tests/{id}

Update a lab test. All fields optional.

### DELETE /api/visit-records/{visitId}/lab-tests/{id}

Delete a lab test.

**Response (204):** No content.

---

## Visit Attachment Endpoints

All visit attachment endpoints require authentication (any role). Attachments are nested under visit records.

### GET /api/visit-records/{visitId}/attachments

List all attachments for a visit record.

**Response (200):** `VisitAttachmentResponse[]`

### POST /api/visit-records/{visitId}/attachments

Upload an attachment. Uses multipart/form-data. Max file size: 100MB.

**Query Parameters:**

| Parameter  | Type | Description |
|------------|------|-------------|
| sourceType | enum | PRESCRIPTION, EXAMINATION, LAB_TEST |
| sourceId   | long | ID of the source entity |

**Request:** `multipart/form-data` with field `file`

**Response (201):** `VisitAttachmentResponse`

### DELETE /api/visit-records/{visitId}/attachments/{id}

Delete an attachment.

**Response (204):** No content.

---

## Visit Invoice Endpoints

All visit invoice endpoints require authentication (any role). Invoices are bound to visit records.

### GET /api/visit-records/{visitId}/invoices

List all invoices bound to a visit record.

**Response (200):** `VisitInvoiceResponse[]`

### POST /api/visit-records/{visitId}/invoices

Bind an invoice to a visit record.

**Request Body:**
```json
{
  "invoiceId": 5,
  "sourceType": "PRESCRIPTION",
  "sourceId": 3
}
```

**Response (201):** `VisitInvoiceResponse`

### DELETE /api/visit-records/{visitId}/invoices/{id}

Unbind an invoice from a visit record. Does not delete the invoice.

**Response (204):** No content.

---

## Subscription Endpoints

All subscription endpoints require authentication (any role).

### GET /api/subscriptions

List subscriptions with server-side pagination, search, and filtering.

**Query Parameters:**

| Parameter  | Type   | Default   | Description                    |
|------------|--------|-----------|--------------------------------|
| search     | string | —         | Search by name                 |
| type       | enum   | —         | PERIODIC, ONE_TIME             |
| status     | enum   | —         | ACTIVE, EXPIRED, CANCELLED     |
| platformId | long   | —         | Filter by platform ID          |
| page       | int    | 0         | Page number                    |
| size       | int    | 10        | Page size                      |
| sortBy     | string | createdAt | Sort field                     |
| sortDir    | string | desc      | Sort direction (asc/desc)      |

**Response (200):** `Page<SubscriptionResponse>`

### GET /api/subscriptions/{id}

Get a subscription by ID with full details and records.

**Response (200):** `SubscriptionDetailResponse`

### POST /api/subscriptions

Create a new subscription.

**Request Body:**
```json
{
  "name": "Netflix",
  "type": "PERIODIC",
  "status": "ACTIVE",
  "platformId": 1,
  "billingMode": "MONTHLY",
  "price": 15.99,
  "description": "Standard plan"
}
```

**Response (201):** `SubscriptionDetailResponse`

### PUT /api/subscriptions/{id}

Update a subscription. All fields optional.

### DELETE /api/subscriptions/{id}

Delete a subscription. Cascades to delete all records.

**Response (204):** No content.

---

## Subscription Record Endpoints

All subscription record endpoints require authentication (any role). Records are nested under subscriptions.

### GET /api/subscriptions/{subId}/records

List all records for a subscription.

**Response (200):** `SubscriptionRecordResponse[]`

### POST /api/subscriptions/{subId}/records

Add a new payment/renewal record.

**Request Body:**
```json
{
  "startDate": "2026-05-01",
  "endDate": "2026-06-01",
  "amount": 15.99,
  "paymentMethodId": 1,
  "note": ""
}
```

**Response (201):** `SubscriptionRecordResponse`

### PUT /api/subscriptions/{subId}/records/{id}

Update a record. All fields optional.

### DELETE /api/subscriptions/{subId}/records/{id}

Delete a record.

**Response (204):** No content.

### POST /api/subscription-records/{id}/attachments

Upload an attachment for a record. Uses multipart/form-data. Max file size: 100MB.

**Request:** `multipart/form-data` with field `file`

**Response (201):** `SubscriptionRecordAttachmentResponse`

### GET /api/subscription-records/{id}/attachments

List attachments for a record.

**Response (200):** `SubscriptionRecordAttachmentResponse[]`

### DELETE /api/subscription-records/{id}/attachments/{attachmentId}

Delete a record attachment.

**Response (204):** No content.

### GET /api/subscription-records/{id}/invoices

List invoices bound to a record.

**Response (200):** `SubscriptionRecordInvoiceResponse[]`

### POST /api/subscription-records/{id}/invoices/{invoiceId}

Bind an invoice to a record.

**Response (201):** No content.

### DELETE /api/subscription-records/{id}/invoices/{invoiceId}

Unbind an invoice from a record. Does not delete the invoice.

**Response (204):** No content.

---

## Payment Method Endpoints

All payment method endpoints require authentication (any role).

### GET /api/payment-methods

List all payment methods.

**Response (200):** `PaymentMethodResponse[]`

```json
[
  {
    "id": 1,
    "name": "微信支付",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

### POST /api/payment-methods

Create a new payment method.

**Request Body:**
```json
{
  "name": "支付宝"
}
```

**Response (201):** `PaymentMethodResponse`

### PUT /api/payment-methods/{id}

Update a payment method. All fields optional.

### DELETE /api/payment-methods/{id}

Delete a payment method. Cannot delete if used by records.

**Response (204):** No content.

---

## Platform Endpoints

All platform endpoints require authentication (any role).

### GET /api/platforms

List all platforms.

**Response (200):** `PlatformResponse[]`

```json
[
  {
    "id": 1,
    "name": "Netflix",
    "website": "https://netflix.com",
    "logoFileId": null,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

### POST /api/platforms

Create a new platform.

**Request Body:**
```json
{
  "name": "Netflix",
  "website": "https://netflix.com",
  "logoFileId": null
}
```

**Response (201):** `PlatformResponse`

### PUT /api/platforms/{id}

Update a platform. All fields optional.

### DELETE /api/platforms/{id}

Delete a platform. Cannot delete if used by subscriptions.

**Response (204):** No content.

---

## Error Responses

| Status | Meaning                |
|--------|------------------------|
| 400    | Validation failed      |
| 401    | Unauthorized           |
| 403    | Forbidden / Not allowed |
| 404    | Resource not found     |
| 409    | Conflict (duplicate)   |
