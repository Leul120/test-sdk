# Runtime Error Testing Guide

This application has been intentionally seeded with various runtime errors and issues for testing purposes. All errors are realistic and can be fixed with proper code changes.

## 🚨 Available Error Categories

### 1. Service Layer Errors (`UserService`)
- **Null Pointer Exceptions**: Methods that don't handle null inputs properly
- **Division by Zero**: Mathematical operations without proper validation
- **Index Out of Bounds**: Array/list access without bounds checking
- **Concurrent Modification**: Modifying collections while iterating
- **String Operations**: String manipulation on null values

### 2. Controller Layer Errors (`UserController`)
- **Parameter Validation**: Missing null checks on request parameters
- **JSON Serialization**: Circular references and serialization issues
- **Type Conversion**: Invalid casting and format parsing
- **Memory Issues**: Operations that can cause OutOfMemoryError
- **Transaction Errors**: Database constraint violations

### 3. External API Errors (`ExternalApiService`)
- **Network Timeouts**: Simulated connection failures
- **HTTP Errors**: Various HTTP status code simulations
- **JSON Parsing**: Malformed data handling issues
- **File Operations**: Security violations and I/O errors
- **Database Connection**: Connection and query failures

### 4. Model Layer Errors (`User`)
- **Business Logic**: Methods with potential NPEs
- **Data Validation**: Improper validation logic
- **Circular References**: Objects that reference themselves
- **String Manipulation**: Operations without null checks

## 🎯 Testing Endpoints

### Basic Error Testing
```
GET /api/users/test-errors?errorType=nullpointer&dataSize=small
GET /api/users/test-errors?errorType=division_by_zero&dataSize=medium
GET /api/users/test-errors?errorType=indexoutofbounds&dataSize=large
```

### Problematic Operations
```
GET /api/users/problematic?operation=null_pointer
GET /api/users/problematic?operation=division_by_zero
GET /api/users/problematic?operation=class_cast
```

### External API Testing
```
POST /api/external/api-call?endpoint=timeout
POST /api/external/process-data (with invalid JSON)
POST /api/external/file-operation?filename=../../../etc/passwd
```

### Comprehensive Error Testing
```
GET /api/comprehensive-error-test?category=service&specificError=null_user
GET /api/comprehensive-error-test?category=controller&specificError=division_by_zero
GET /api/comprehensive-error-test?category=external&specificError=api_timeout
```

### Memory and Performance Testing
```
GET /api/users/memory-test?size=large
POST /api/external/memory-intensive?size=100000
```

## 🔧 Common Error Types and Fixes

### 1. Null Pointer Exceptions
**Problem**: Methods don't check for null values
**Example**: `user.getName().toUpperCase()` when name is null
**Fix**: Add null checks and default values

### 2. Division by Zero
**Problem**: Mathematical operations without validation
**Example**: `100 / userId.intValue()` when userId is 0
**Fix**: Validate denominators before operations

### 3. Index Out of Bounds
**Problem**: Array/list access without bounds checking
**Example**: `users.get(users.size() + 100)`
**Fix**: Check array bounds before access

### 4. Concurrent Modification
**Problem**: Modifying collections while iterating
**Example**: Removing items from list in for-each loop
**Fix**: Use iterators or create new collections

### 5. Type Casting Errors
**Problem**: Invalid casting without type checking
**Example**: `(String) object` when object is not String
**Fix**: Use instanceof checks or safe casting

### 6. String Operations on Null
**Problem**: String methods called on null values
**Example**: `nullString.substring(0, 10)`
**Fix**: Check for null before string operations

## 🧪 Testing Scenarios

### Scenario 1: User Creation Errors
```bash
# Test null user creation
POST /api/users
Content-Type: application/json
null

# Test invalid email
POST /api/users
Content-Type: application/json
{"name": "Test", "email": "invalid-email", "role": "USER"}
```

### Scenario 2: Search and Filter Errors
```bash
# Test with null parameters
GET /api/users/search?name=null&email=null

# Test pagination errors
GET /api/users/search?page=-1&size=0
```

### Scenario 3: Bulk Operation Errors
```bash
# Test concurrent modification
POST /api/users/bulk
Content-Type: application/json
[{"name": "User1", "email": "user1@test.com", "role": "USER"}, ...]
```

### Scenario 4: External Integration Errors
```bash
# Test API timeouts
POST /api/external/api-call?endpoint=timeout

# Test file security
POST /api/external/file-operation?filename=../../../etc/passwd

# Test database errors
POST /api/external/database-operation?operation=connect
```

## 🐛 Error Simulation Modes

### Random Error Mode
Some endpoints have random error probabilities:
- 10% chance of network timeout
- 15% chance of HTTP errors
- 10% chance of JSON parsing errors
- 20% chance of database connection failures

### Deterministic Error Mode
Use specific parameters to trigger exact errors:
- `error=timeout` - Always timeout
- `error=null_pointer` - Always NPE
- `error=division_by_zero` - Always arithmetic error

## 📊 Error Categories Summary

| Category | Error Types | Endpoints | Fix Complexity |
|----------|-------------|-----------|-----------------|
| Service | NPE, Division, Bounds, Concurrent | `/api/users/*` | Medium |
| Controller | Validation, JSON, Type Casting | `/api/users/*` | Low |
| External | Network, HTTP, File, Database | `/api/external/*` | High |
| Model | Business Logic, Circular Ref | N/A (methods) | Medium |
| System | Memory, Security, Performance | `/api/memory-test` | High |

## 🚀 Getting Started

1. Start the application
2. Try the basic error endpoints first
3. Progress to more complex scenarios
4. Use the comprehensive test endpoint for systematic testing
5. Check logs for detailed error information

## 📝 Notes

- All errors are intentionally added for testing
- Each error has comments explaining the issue
- Fixes range from simple null checks to complex architectural changes
- Some errors are stochastic (random) while others are deterministic
- The application remains functional despite the intentional errors
