# Test Requests to Trigger Runtime Errors

## 1. User Search Controller - Trigger IllegalArgumentException
**Request:** `GET /api/users/search?size=0`
**Error:** IllegalArgumentException - "Page size must be greater than 0"
**Why it fails:** Division by zero scenario in pagination logic

```bash
curl -X GET "http://localhost:8085/api/users/search?size=0" -H "Content-Type: application/json"
```

## 2. Bulk User Creation - Trigger OutOfMemoryError
**Request:** `POST /api/users/bulk` with 1001+ users
**Error:** IllegalArgumentException - "Cannot create more than 1000 users in a single request"
**Why it fails:** Exceeds the size limit designed to prevent memory issues

```bash
curl -X POST "http://localhost:8085/api/users/bulk" \
  -H "Content-Type: application/json" \
  -d '[
    {"name":"User1","email":"user1@test.com","role":"USER"},
    {"name":"User2","email":"user2@test.com","role":"USER"},
    {"name":"User3","email":"user3@test.com","role":"USER"},
    {"name":"User4","email":"user4@test.com","role":"USER"},
    {"name":"User5","email":"user5@test.com","role":"USER"}
    // ... add 996 more users to exceed 1000 limit
  ]'
```

## 3. User Analytics - Trigger IllegalStateException
**Request:** `GET /api/users/analytics` (when no users exist)
**Error:** IllegalStateException - "No users found for analytics"  
**Why it fails:** Division by zero when calculating percentages with empty user list

```bash
curl -X GET "http://localhost:8085/api/users/analytics" -H "Content-Type: application/json"
```

## Alternative Request for Export Controller - Trigger ClassCastException
**Request:** `GET /api/users/export?format=invalid`
**Error:** IllegalArgumentException - "Unsupported export format: invalid"
**Why it fails:** Invalid format parameter causes switch statement to throw exception

```bash
curl -X GET "http://localhost:8085/api/users/export?format=invalid" -H "Content-Type: application/json"
```

## Quick Test Script (3 failures):

```bash
# 1. Trigger IllegalArgumentException in search
echo "1. Testing search with size=0..."
curl -X GET "http://localhost:8085/api/users/search?size=0"

# 2. Trigger IllegalStateException in analytics  
echo "2. Testing analytics with empty database..."
curl -X GET "http://localhost:8085/api/users/analytics"

# 3. Trigger IllegalArgumentException in export
echo "3. Testing export with invalid format..."
curl -X GET "http://localhost:8085/api/users/export?format=invalid"
```

## Additional Test - Trigger NullPointerException:
**Request:** `GET /api/users/search?name=null`
**Error:** NullPointerException in stream filtering
**Why it fails:** Null value handling in string comparison

```bash
curl -X GET "http://localhost:8085/api/users/search?name=null" -H "Content-Type: application/json"
```
