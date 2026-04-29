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

## Error Responses

| Status | Meaning                |
|--------|------------------------|
| 400    | Validation failed      |
| 401    | Unauthorized           |
| 403    | Forbidden / Not allowed |
| 404    | Resource not found     |
| 409    | Conflict (duplicate)   |
