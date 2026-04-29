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
| status     | String  | null        | Filter by status: EXPIRED, EXPIRING_SOON, IN_USE, EXHAUSTED |
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

Create a new item. Provide exactly 2 of the 3 date fields (`productDate`, `expirationDate`, `lifeDays`) — the third is auto-calculated.

**Request Body (example with productDate + lifeDays):**
```json
{
  "productDate": "2026-01-01",
  "lifeDays": 181,
  "inUse": true
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

**Response (201):**
```json
{
  "id": 1,
  "productDate": "2026-01-01",
  "expirationDate": "2026-07-01",
  "lifeDays": 181,
  "inUse": true,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-01-01T00:00:00"
}
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

## Good Picture Endpoints

All good picture endpoints require authentication (any role). Pictures are nested under goods. Accepted file types: JPEG, PNG, WebP, GIF. Max file size: 10MB.

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

**Response (400):**
```json
{
  "message": "File type not allowed. Accepted types: JPEG, PNG, WebP, GIF"
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

## Error Responses

| Status | Meaning                |
|--------|------------------------|
| 400    | Validation failed      |
| 401    | Unauthorized           |
| 403    | Forbidden / Not allowed |
| 404    | Resource not found     |
| 409    | Conflict (duplicate)   |
