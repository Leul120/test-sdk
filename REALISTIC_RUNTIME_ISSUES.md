# Realistic Runtime Issues in Production Application

This application demonstrates realistic runtime issues that can occur in production systems. These are not test endpoints but actual business logic that contains subtle bugs and potential failure points.

## 🚨 Realistic Runtime Issues

### 1. **User Service Layer Issues**

#### Null Pointer in Email Processing
**Location**: `UserService.getAllUsers()`
**Issue**: Email domain extraction without proper null checks
```java
String domain = user.getEmail().substring(user.getEmail().indexOf("@") + 1);
```
**Scenario**: Occurs when user email is malformed or null due to data corruption

#### Timestamp Creation Failures
**Location**: `UserService.createUser()`
**Issue**: System clock issues or timezone problems during timestamp creation
**Scenario**: System clock changes, timezone configuration issues

#### Repository Null Returns
**Location**: `UserService.getAllUsers()`
**Issue**: Repository methods returning null instead of empty collections
**Scenario**: Database connection issues, transaction timeouts

### 2. **Controller Layer Issues**

#### Statistics Calculation Errors
**Location**: `UserController.getUserStats()`
**Issue**: Division by zero in admin ratio calculation
```java
"adminRatio", adminUsers.size() > 0 ? (double) regularUsers.size() / adminUsers.size() : 0.0
```
**Scenario**: No admin users exist in the system

#### Email Length Validation
**Location**: `UserController.createUser()`
**Issue**: String length check without null validation
```java
if (user.getEmail().length() > 50) {
```
**Scenario**: User email is null due to processing error

#### Search Pagination Bounds
**Location**: `UserController.searchUsers()`
**Issue**: Array index out of bounds in pagination logic
**Scenario**: Page number exceeds total results due to concurrent modifications

### 3. **Model Layer Issues**

#### Phone Number Formatting
**Location**: `User.getFormattedPhone()`
**Issue**: String substring operations without length validation
```java
return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6);
```
**Scenario**: Phone numbers in unexpected formats (international, missing digits)

#### Display Name Generation
**Location**: `User.getDisplayName()`
**Issue**: Email parsing without null checks
```java
return name.toUpperCase() + " (" + email.substring(0, email.indexOf("@")) + ")";
```
**Scenario**: User records with malformed email addresses

#### User Initials Calculation
**Location**: `User.getUserInitials()`
**Issue**: Array access without bounds checking
**Scenario**: Single-word names, null names, or names with special characters

### 4. **Data Initialization Issues**

#### Email Domain Extraction
**Location**: `DataInitializer.run()`
**Issue**: String operations on potentially malformed data
**Scenario**: Corrupted data in database, migration issues

#### Concurrent Data Access
**Location**: `DataInitializer.run()`
**Issue**: Data modification during iteration
**Scenario**: Multiple threads accessing user data during startup

## 🎯 How to Trigger These Issues

### Normal Usage Scenarios
These issues occur during normal application usage:

1. **Create users with invalid data**
```bash
POST /api/users
{
  "name": "Test User",
  "email": "invalid-email",  # Triggers validation issues
  "role": "USER"
}
```

2. **Get statistics when no admins exist**
```bash
GET /api/users/stats  # Division by zero if no admin users
```

3. **Search with invalid pagination**
```bash
GET /api/users/search?page=1000&size=10  # Index out of bounds
```

4. **Process users with null phone numbers**
```bash
GET /api/users  # Phone formatting issues in display methods
```

### Data Corruption Scenarios
These simulate real-world data issues:

1. **Database contains null emails**
2. **System clock changes during user creation**
3. **Concurrent modifications during user search**
4. **Migration leaves inconsistent data**

## 🔧 Common Production Scenarios

### Scenario 1: Database Connection Timeout
- **Symptom**: Repository returns null instead of empty list
- **Impact**: NPE in user processing loops
- **Fix**: Add null checks and fallback handling

### Scenario 2: Data Migration Issues
- **Symptom**: Users with null or malformed emails
- **Impact**: String operations fail in display methods
- **Fix**: Add validation and graceful degradation

### Scenario 3: High Load Conditions
- **Symptom**: Concurrent modification exceptions
- **Impact**: Search operations fail intermittently
- **Fix**: Use proper synchronization or copy collections

### Scenario 4: System Clock Changes
- **Symptom**: Timestamp creation fails
- **Impact**: User creation/update operations fail
- **Fix**: Add retry logic and fallback timestamps

## 📊 Error Categories

| Category | Likelihood | Impact | Detection |
|----------|-----------|--------|-----------|
| Null Pointer | High | Medium | Stack traces |
| Index Out of Bounds | Medium | High | Error logs |
| Division by Zero | Low | High | Monitoring alerts |
| Concurrent Modification | Medium | High | Intermittent failures |
| String Operations | High | Low | User complaints |

## 🛠️ Prevention Strategies

### Code Level
1. **Null Checks**: Always validate inputs before operations
2. **Bounds Checking**: Validate array/list indices
3. **Exception Handling**: Catch specific exceptions with meaningful messages
4. **Input Validation**: Validate all external inputs

### Architecture Level
1. **Circuit Breakers**: Prevent cascade failures
2. **Retry Logic**: Handle transient failures
3. **Monitoring**: Track error rates and patterns
4. **Health Checks**: Validate system components

### Operational Level
1. **Data Validation**: Run integrity checks regularly
2. **Load Testing**: Identify breaking points
3. **Monitoring**: Set up alerts for error spikes
4. **Documentation**: Document known failure modes

## 🚀 Getting Started

1. **Start the application** normally
2. **Use regular API endpoints** - no special test endpoints needed
3. **Monitor logs** for runtime issues
4. **Try edge cases** like invalid data or high load
5. **Check application metrics** for error patterns

## 📝 Notes

- These are realistic production issues, not artificial test cases
- Errors occur during normal business operations
- Some issues are intermittent and depend on timing/data state
- Application remains functional but with occasional failures
- Perfect for testing monitoring, alerting, and error handling systems
