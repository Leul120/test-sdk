# AI Synapse SDK Testing Guide

This e-commerce demo application is designed to naturally trigger various runtime issues that the AI Synapse SDK can detect and analyze. Below are the testing scenarios for each issue type.

## 🚨 Critical Priority Issues

### 1. SECURITY_SIGNAL
**Endpoints:**
- `POST /api/chaos/security/xss` - Test XSS detection
- `POST /api/chaos/security/sql-injection` - Test SQL injection detection

**Examples:**
```bash
# XSS Test
curl -X POST http://localhost:8085/api/chaos/security/xss \
  -H "Content-Type: application/json" \
  -d "<script>alert('xss')</script>"

# SQL Injection Test  
curl -X POST http://localhost:8085/api/chaos/security/sql-injection \
  -H "Content-Type: application/json" \
  -d "'; DROP TABLE users; --"
```

## 🔴 High Priority Issues

### 2. ERROR_THRESHOLD
**Endpoint:** `POST /api/chaos/errors/generate?count=7`

**Trigger:** Generate 5+ errors within time window
```bash
curl -X POST "http://localhost:8085/api/chaos/errors/generate?count=7"
```

### 3. LATENCY_SPIKE  
**Endpoint:** `POST /api/chaos/latency/spike?durationMs=1500`

**Trigger:** Average latency > 1000ms
```bash
curl -X POST "http://localhost:8085/api/chaos/latency/spike?durationMs=1500"
```

## 🟡 Medium Priority Issues

### 4. STATUS_CODE_ISSUES
**Endpoint:** `GET /api/chaos/status/{code}`

**Trigger:** 10+ non-2xx status codes
```bash
# Generate multiple 4xx/5xx responses
for i in {1..15}; do
  curl -X GET "http://localhost:8085/api/chaos/status/500"
  curl -X GET "http://localhost:8085/api/chaos/status/404"
done
```

### 5. MEMORY_PRESSURE
**Endpoint:** `POST /api/chaos/memory/pressure?percentage=85`

**Trigger:** Memory usage > 80%
```bash
curl -X POST "http://localhost:8085/api/chaos/memory/pressure?percentage=85"
```

### 6. CPU_PRESSURE
**Endpoint:** `POST /api/chaos/cpu/pressure?percentage=80`

**Trigger:** CPU usage > 75%
```bash
curl -X POST "http://localhost:8085/api/chaos/cpu/pressure?percentage=80"
```

### 7. CONNECTION_POOL_EXHAUSTION
**Endpoint:** `POST /api/chaos/connections/exhaust?count=95`

**Trigger:** Connection pool usage > 90%
```bash
curl -X POST "http://localhost:8085/api/chaos/connections/exhaust?count=95"
```

### 8. DATABASE_CONNECTION_ISSUES
**Endpoints:**
- `POST /api/chaos/database/timeout` - Simulate connection timeout
- `POST /api/chaos/database/deadlock` - Simulate deadlock

```bash
curl -X POST http://localhost:8085/api/chaos/database/timeout
curl -X POST http://localhost:8085/api/chaos/database/deadlock
```

## 🟢 Low Priority Issues

### 9. CACHE_PERFORMANCE_ISSUES
**Endpoint:** `POST /api/chaos/cache/poor-performance`

**Trigger:** Hit rate < 50%, miss rate > 30%, evictions > 1000
```bash
curl -X POST http://localhost:8085/api/chaos/cache/poor-performance
```

### 10. TIMEOUT_ISSUES
**Endpoint:** `POST /api/chaos/timeout/simulate?timeoutMs=5000`

**Trigger:** Timeout-related errors
```bash
curl -X POST "http://localhost:8085/api/chaos/timeout/simulate?timeoutMs=5000"
```

### 11. THROUGHPUT_DEGRADATION
**Endpoint:** `POST /api/chaos/throughput/degrade?rps=0.5`

**Trigger:** Request rate < 1 RPS (requires 10+ events)
```bash
curl -X POST "http://localhost:8085/api/chaos/throughput/degrade?rps=0.5"
```

### 12. REPEATED_ERROR
**Endpoint:** `POST /api/chaos/errors/repeated`

**Trigger:** Same error message appears 3+ times
```bash
curl -X POST "http://localhost:8085/api/chaos/errors/repeated" \
  -H "Content-Type: application/json" \
  -d "Database connection failed"
```

## 🛒 E-commerce Operations (Real-world Scenarios)

### Normal Operations that can trigger issues:
1. **Product Search:** `POST /api/ecommerce/products/search`
   - Can trigger security signals with malicious input
   - Can cause latency with complex queries

2. **Order Creation:** `POST /api/ecommerce/orders`
   - Can cause database connection issues
   - Can trigger timeouts under load
   - May cause connection pool exhaustion

3. **Memory/CPU Stress:** 
   - `POST /api/ecommerce/stress/memory?sizeMB=500`
   - `POST /api/ecommerce/stress/cpu?iterations=10000000`

## 📊 Monitoring Endpoints

### Health Check:
```bash
curl -X GET http://localhost:8085/api/ecommerce/health
```

### Actuator Endpoints:
```bash
curl -X GET http://localhost:8085/api/actuator/health
curl -X GET http://localhost:8085/api/actuator/metrics
curl -X GET http://localhost:8085/api/actuator/prometheus
```

## 🔧 Load Testing Script

Use this script to generate load and trigger multiple issues:

```bash
#!/bin/bash
# Generate mixed load to trigger various issues

echo "Starting load test..."

# Generate errors for threshold
for i in {1..7}; do
  curl -X POST "http://localhost:8085/api/chaos/errors/generate?count=1" &
done

# Generate latency spikes
curl -X POST "http://localhost:8085/api/chaos/latency/spike?durationMs=1500" &

# Create memory pressure
curl -X POST "http://localhost:8085/api/chaos/memory/pressure?percentage=85" &

# Create CPU pressure
curl -X POST "http://localhost:8085/api/chaos/cpu/pressure?percentage=80" &

# Generate status code issues
for i in {1..12}; do
  curl -X GET "http://localhost:8085/api/chaos/status/500" &
  curl -X GET "http://localhost:8085/api/chaos/status/404" &
done

# Simulate database issues
curl -X POST http://localhost:8085/api/chaos/database/timeout &
curl -X POST http://localhost:8085/api/chaos/database/deadlock &

# Normal e-commerce load
for i in {1..20}; do
  curl -X GET http://localhost:8085/api/ecommerce/products &
  curl -X POST http://localhost:8085/api/ecommerce/products/search \
    -H "Content-Type: application/json" \
    -d '{"query":"laptop"}' &
done

wait
echo "Load test completed!"
```

## 📝 Testing Checklist

- [ ] Test SECURITY_SIGNAL with XSS and SQL injection
- [ ] Trigger ERROR_THRESHOLD with 5+ errors
- [ ] Create LATENCY_SPIKE > 1000ms
- [ ] Generate 10+ STATUS_CODE_ISSUES
- [ ] Exceed MEMORY_PRESSURE threshold (>80%)
- [ ] Exceed CPU_PRESSURE threshold (>75%)
- [ ] Exhaust CONNECTION_POOL (>90%)
- [ ] Simulate DATABASE_CONNECTION_ISSUES
- [ ] Create CACHE_PERFORMANCE_ISSUES
- [ ] Trigger TIMEOUT_ISSUES
- [ ] Cause THROUGHPUT_DEGRADATION
- [ ] Generate REPEATED_ERROR patterns

This comprehensive testing setup will help validate that your AI Synapse SDK can detect and analyze all the specified runtime issues in a realistic e-commerce environment.
