# AI-Synapse SDK Spring Boot Demo

A complete Spring Boot application demonstrating the AI-Synapse Java SDK functionality.

## Features

- **REST API Endpoints**: Sample endpoints for users, products, errors, and slow responses
- **Automatic Request Tracking**: Filter intercepts all API calls automatically
- **Log Simulator**: Scheduled tasks generate varied events for testing
- **Error Tracking**: Demonstrates error capture with context

## Prerequisites

1. **Build the SDK locally** (if not already done):
   ```bash
   cd ../aisynapse-sdk-java
   mvn clean install -DskipTests
   ```

2. **Start AI-Synapse services**:
   ```bash
   cd ..
   docker compose up -d postgres kafka zookeeper kafka-init aisynapse-core
   ```

## Running the Demo

```bash
cd sdk-spring-boot-demo
mvn spring-boot:run
```

The app runs on **http://localhost:8085**

## Testing Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/demo/health` | GET | Health check |
| `/api/demo/users` | GET | List all users |
| `/api/demo/users` | POST | Create a user |
| `/api/demo/products/{id}` | GET | Get product by ID |
| `/api/demo/error` | GET | Trigger error for testing |
| `/api/demo/slow` | GET | Slow response (500-1000ms) |
| `/api/demo/batch-test` | GET | Send batch of events |

## Quick Test Commands

```bash
# Health check
curl http://localhost:8085/api/demo/health

# Get users
curl http://localhost:8085/api/demo/users

# Create user
curl -X POST http://localhost:8085/api/demo/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com"}'

# Get product
curl http://localhost:8085/api/demo/products/101

# Trigger error
curl http://localhost:8085/api/demo/error

# Slow endpoint
curl http://localhost:8085/api/demo/slow
```

## What Gets Sent to AI-Synapse

Every request tracked includes:
- **source**: Application identifier (e.g., `sdk-demo-app`)
- **endpoint**: The API path
- **method**: HTTP method (GET, POST, etc.)
- **statusCode**: Response status
- **latencyMs**: Request duration in milliseconds
- **payload**: Optional additional context

## Log Simulator

The app automatically generates events:
- **Every 5 seconds**: Random API events
- **Every 30 seconds**: Batch of 3-7 events
- **Every 45 seconds**: Simulated error events

Check the console logs to see events being sent!

## Configuration

Edit `src/main/resources/application.yml` to change:

```yaml
aisynapse:
  core-url: http://localhost:8080      # AI-Synapse Core URL
  transport: HTTP                       # HTTP, KAFKA, or BOTH
  debug: true                           # Enable debug logging
```
