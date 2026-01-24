# AI-Synapse SDK Demo API

A fully functional Spring Boot application demonstrating AI-Synapse SDK monitoring capabilities.

## Features

- **User Management**: Complete CRUD operations for user entities
- **Validation**: Input validation with proper error handling
- **Business Logic**: Role-based operations, user activation/deactivation
- **Error Scenarios**: Multiple endpoints for testing error handling
- **Automatic Monitoring**: AI-Synapse SDK captures all API calls automatically

## API Endpoints

### User Operations
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/by-email?email={email}` - Get user by email
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update existing user
- `DELETE /api/users/{id}` - Delete user
- `PATCH /api/users/{id}/activate` - Activate user
- `PATCH /api/users/{id}/deactivate` - Deactivate user
- `GET /api/users/by-role?role={role}` - Get users by role
- `GET /api/users/stats` - Get user statistics

### Testing Endpoints
- `GET /api/users/error` - Triggers a runtime error
- `POST /api/users/invalid` - Tests validation errors
- `POST /api/users/{id}/operations?operation={op}` - Complex operations (10% failure rate)
- `GET /api/health` - Health check

## Sample Usage

### Create a User
```bash
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "USER",
    "phone": "+1234567890",
    "department": "Engineering"
  }'
```

### Get All Users
```bash
curl http://localhost:8085/api/users
```

### Trigger Error (for testing)
```bash
curl http://localhost:8085/api/users/error
```

### Complex Operation (may fail)
```bash
curl -X POST http://localhost:8085/api/users/1/operations?operation=promote_to_admin
```

## Monitoring

All endpoints are automatically monitored by AI-Synapse SDK:
- HTTP method, status code, and latency captured
- Full stack traces for errors
- Request/response metadata
- No manual annotations required

## Error Scenarios for Testing

1. **Validation Errors**: Invalid email, empty name, invalid role
2. **Business Logic Errors**: Duplicate email, invalid user ID
3. **Runtime Errors**: Deliberate exceptions, simulated failures
4. **Complex Operations**: 10% random failure rate

This provides rich data for testing AI-Synapse's suggestion capabilities.
