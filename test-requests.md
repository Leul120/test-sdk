# Test Requests to Trigger Runtime Errors

## NEW: Comprehensive Error Testing Endpoint
**Endpoint:** `GET /api/users/test-errors`
**Parameters:** `errorType` (required), `dataSize` (optional), `customMessage` (optional)

This single endpoint can trigger 17 different types of errors with various data sizes!

### Available Error Types:
- `nullpointer` - NullPointerException
- `illegalargument` - IllegalArgumentException  
- `illegalstate` - IllegalStateException
- `arithmetic` - ArithmeticException (division by zero)
- `indexoutofbounds` - IndexOutOfBoundsException
- `classcast` - ClassCastException
- `numberformat` - NumberFormatException
- `outofmemory` - OutOfMemoryError
- `stackoverflow` - StackOverflowError
- `runtime` - RuntimeException
- `json_serialization` - JSON serialization issues
- `stream_error` - Stream processing errors
- `concurrent_modification` - ConcurrentModificationException
- `reflection` - Reflection errors
- `io_error` - IO errors
- `security` - Security errors
- `custom` - Custom business logic errors

### Available Data Sizes:
- `tiny` (1 item)
- `small` (10 items) 
- `medium` (100 items)
- `large` (1000 items)
- `huge` (10000 items)

---

## Quick Test Examples:

### 1. Trigger NullPointerException with large dataset
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=nullpointer&dataSize=large"
```

### 2. Trigger ArithmeticException (division by zero)
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=arithmetic&dataSize=medium"
```

### 3. Trigger OutOfMemoryError
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=outofmemory&dataSize=large"
```

### 4. Trigger StackOverflowError
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=stackoverflow"
```

### 5. Trigger ConcurrentModificationException
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=concurrent_modification&dataSize=huge"
```

### 6. Trigger JSON serialization circular reference error
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=json_serialization&dataSize=small"
```

### 7. Trigger custom error with message
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=custom&customMessage=My custom error message"
```

### 8. Trigger ClassCastException
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=classcast&dataSize=small"
```

### 9. Trigger NumberFormatException
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=numberformat&dataSize=large"
```

### 10. Trigger Security error
```bash
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=security"
```

---

## Comprehensive Test Script (All Error Types):

```bash
#!/bin/bash
BASE_URL="http://localhost:8080/api/users/test-errors"

echo "=== Comprehensive Error Testing ==="

# Test all error types with different data sizes
echo "1. NullPointerException (large dataset)..."
curl -X GET "$BASE_URL?errorType=nullpointer&dataSize=large"

echo -e "\n2. IllegalArgumentException (medium dataset)..."
curl -X GET "$BASE_URL?errorType=illegalargument&dataSize=medium&customMessage=Invalid user data provided"

echo -e "\n3. IllegalStateException (small dataset)..."
curl -X GET "$BASE_URL?errorType=illegalstate&dataSize=small"

echo -e "\n4. ArithmeticException (tiny dataset)..."
curl -X GET "$BASE_URL?errorType=arithmetic&dataSize=tiny"

echo -e "\n5. IndexOutOfBoundsException (medium dataset)..."
curl -X GET "$BASE_URL?errorType=indexoutofbounds&dataSize=medium"

echo -e "\n6. ClassCastException (small dataset)..."
curl -X GET "$BASE_URL?errorType=classcast&dataSize=small"

echo -e "\n7. NumberFormatException (large dataset)..."
curl -X GET "$BASE_URL?errorType=numberformat&dataSize=large"

echo -e "\n8. OutOfMemoryError (small dataset)..."
curl -X GET "$BASE_URL?errorType=outofmemory&dataSize=small"

echo -e "\n9. StackOverflowError..."
curl -X GET "$BASE_URL?errorType=stackoverflow"

echo -e "\n10. RuntimeException with custom message..."
curl -X GET "$BASE_URL?errorType=runtime&customMessage=This is a test runtime exception"

echo -e "\n11. JSON Serialization Error (medium dataset)..."
curl -X GET "$BASE_URL?errorType=json_serialization&dataSize=medium"

echo -e "\n12. Stream Processing Error (large dataset)..."
curl -X GET "$BASE_URL?errorType=stream_error&dataSize=large"

echo -e "\n13. ConcurrentModificationException (huge dataset)..."
curl -X GET "$BASE_URL?errorType=concurrent_modification&dataSize=huge"

echo -e "\n14. Reflection Error..."
curl -X GET "$BASE_URL?errorType=reflection"

echo -e "\n15. IO Error..."
curl -X GET "$BASE_URL?errorType=io_error"

echo -e "\n16. Security Error..."
curl -X GET "$BASE_URL?errorType=security"

echo -e "\n17. Custom Business Logic Error..."
curl -X GET "$BASE_URL?errorType=custom&customMessage=Business rule violation detected"

echo -e "\n=== Testing Complete ==="
```

---

## Stress Testing with Massive Data:

### Test with 10,000 records + multiple error types:
```bash
# Huge dataset + NullPointerException
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=nullpointer&dataSize=huge"

# Huge dataset + Stream errors  
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=stream_error&dataSize=huge"

# Huge dataset + ConcurrentModification
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=concurrent_modification&dataSize=huge"
```

---

## View Available Options:
```bash
# See all available error types and data sizes
curl -X GET "http://localhost:8080/api/users/test-errors?errorType=none"
```

---

## Legacy Tests (Original Endpoints):

### 1. User Search Controller - Trigger IllegalArgumentException
**Request:** `GET /api/users/search?size=0`
**Error:** IllegalArgumentException - "Page size must be greater than 0"

```bash
curl -X GET "http://localhost:8080/api/users/search?size=0"
```

### 2. User Analytics - Trigger IllegalStateException  
**Request:** `GET /api/users/analytics` (when no users exist)
**Error:** IllegalStateException - "No users found for analytics"

```bash
curl -X GET "http://localhost:8080/api/users/analytics"
```

### 3. Export Controller - Trigger IllegalArgumentException
**Request:** `GET /api/users/export?format=invalid`
**Error:** IllegalArgumentException - "Unsupported export format: invalid"

```bash
curl -X GET "http://localhost:8080/api/users/export?format=invalid"
```
