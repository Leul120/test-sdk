from flask import Flask, request, jsonify, render_template_string
import sqlite3
import time
import random
import json
import hashlib
import threading
import psutil
import os
from datetime import datetime
import logging

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Global variables for simulating issues
error_count = 0
request_times = []
memory_leak_data = []

# Database setup
def init_db():
    conn = sqlite3.connect('app.db')
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY,
            username TEXT,
            email TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS products (
            id INTEGER PRIMARY KEY,
            name TEXT,
            price REAL,
            description TEXT
        )
    ''')
    # Insert sample data
    cursor.execute("INSERT OR IGNORE INTO users (id, username, email) VALUES (1, 'admin', 'admin@test.com')")
    cursor.execute("INSERT OR IGNORE INTO users (id, username, email) VALUES (2, 'user1', 'user1@test.com')")
    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, description) VALUES (1, 'Laptop', 999.99, 'High-performance laptop')")
    cursor.execute("INSERT OR IGNORE INTO products (id, name, price, description) VALUES (2, 'Phone', 699.99, 'Smartphone with great camera')")
    conn.commit()
    conn.close()

@app.route('/')
def index():
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>E-Commerce Demo App</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 40px; }
            .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 5px; }
            .warning { background: #fff3cd; padding: 10px; border-radius: 5px; margin: 10px 0; }
        </style>
    </head>
    <body>
        <h1>E-Commerce Demo Application</h1>
        <div class="warning">
            <strong>⚠️ This app intentionally contains vulnerabilities and performance issues for monitoring demonstrations</strong>
        </div>
        
        <h2>Available Endpoints:</h2>
        <div class="endpoint">
            <strong>GET /search?q=&lt;query&gt;</strong> - Search products (vulnerable to XSS)
        </div>
        <div class="endpoint">
            <strong>GET /user/&lt;id&gt;</strong> - Get user info (vulnerable to SQL injection)
        </div>
        <div class="endpoint">
            <strong>POST /login</strong> - Login (vulnerable to authentication bypass)
        </div>
        <div class="endpoint">
            <strong>GET /slow</strong> - Simulates slow response (latency issues)
        </div>
        <div class="endpoint">
            <strong>GET /error</strong> - Intentionally throws errors (error threshold testing)
        </div>
        <div class="endpoint">
            <strong>GET /memory-leak</strong> - Causes memory leak (memory pressure)
        </div>
        <div class="endpoint">
            <strong>GET /cpu-intensive</strong> - CPU intensive operation (CPU pressure)
        </div>
        <div class="endpoint">
            <strong>GET /db-timeout</strong> - Database timeout simulation
        </div>
        <div class="endpoint">
            <strong>GET /cache-miss</strong> - Cache performance issues
        </div>
        <div class="endpoint">
            <strong>GET /connection-pool</strong> - Connection pool exhaustion
        </div>
    </body>
    </html>
    ''')

# SECURITY_SIGNAL - SQL Injection vulnerability
@app.route('/user/<user_id>')
def get_user(user_id):
    global error_count
    start_time = time.time()
    
    try:
        # Vulnerable to SQL injection
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        
        # Direct string interpolation - SQL injection vulnerability
        query = f"SELECT * FROM users WHERE id = {user_id}"
        logger.info(f"Executing query: {query}")
        
        cursor.execute(query)
        user = cursor.fetchone()
        conn.close()
        
        request_times.append(time.time() - start_time)
        
        if user:
            return jsonify({
                'id': user[0],
                'username': user[1],
                'email': user[2],
                'created_at': user[3]
            })
        else:
            return jsonify({'error': 'User not found'}), 404
            
    except Exception as e:
        error_count += 1
        logger.error(f"Database error: {str(e)}")
        return jsonify({'error': f'Database error: {str(e)}'}), 500

# SECURITY_SIGNAL - XSS vulnerability
@app.route('/search')
def search():
    global error_count
    start_time = time.time()
    
    try:
        query = request.args.get('q', '')
        
        # Vulnerable to XSS - direct reflection
        if not query:
            return render_template_string('''
                <html>
                <body>
                    <h2>Search Products</h2>
                    <form method="GET">
                        <input type="text" name="q" placeholder="Search..." style="width: 300px;">
                        <button type="submit">Search</button>
                    </form>
                </body>
                </html>
            ''')
        
        # Simulate search with XSS vulnerability
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM products WHERE name LIKE ?", (f'%{query}%',))
        products = cursor.fetchall()
        conn.close()
        
        request_times.append(time.time() - start_time)
        
        # XSS vulnerability - direct reflection of user input
        return render_template_string(f'''
            <html>
            <body>
                <h2>Search Results for: {query}</h2>
                <form method="GET">
                    <input type="text" name="q" value="{query}" style="width: 300px;">
                    <button type="submit">Search</button>
                </form>
                <h3>Products Found:</h3>
                {"<br>".join([f"{p[1]} - ${p[2]}" for p in products]) if products else "No products found"}
            </body>
            </html>
        ''')
        
    except Exception as e:
        error_count += 1
        return jsonify({'error': f'Search error: {str(e)}'}), 500

# SECURITY_SIGNAL - Authentication bypass vulnerability
@app.route('/login', methods=['POST'])
def login():
    global error_count
    start_time = time.time()
    
    try:
        data = request.get_json() or {}
        username = data.get('username', '')
        password = data.get('password', '')
        
        # Vulnerable authentication logic
        if username == 'admin' and password == 'admin':
            return jsonify({'token': 'admin-token-123', 'user': 'admin'})
        
        # Vulnerable to bypass with empty password
        if not password:
            return jsonify({'token': 'bypass-token', 'user': username})
        
        # Simulate database check with vulnerability
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM users WHERE username = ?", (username,))
        user = cursor.fetchone()
        conn.close()
        
        request_times.append(time.time() - start_time)
        
        if user:
            return jsonify({'token': f'token-{username}', 'user': username})
        else:
            return jsonify({'error': 'Invalid credentials'}), 401
            
    except Exception as e:
        error_count += 1
        return jsonify({'error': f'Login error: {str(e)}'}), 500

# LATENCY_SPIKE - Slow endpoint
@app.route('/slow')
def slow_endpoint():
    global error_count
    start_time = time.time()
    
    # Simulate slow processing
    delay = random.uniform(0.5, 2.0)  # 500ms to 2s delay
    time.sleep(delay)
    
    # Add some CPU work
    result = sum(i * i for i in range(10000))
    
    request_times.append(time.time() - start_time)
    
    return jsonify({
        'message': 'This endpoint is intentionally slow',
        'delay_seconds': delay,
        'computation_result': result,
        'processing_time': time.time() - start_time
    })

# ERROR_THRESHOLD - Error generating endpoint
@app.route('/error')
def error_endpoint():
    global error_count
    error_count += 1
    
    # Randomly generate different types of errors
    error_types = [
        {'type': 'ValueError', 'message': 'Invalid input value'},
        {'type': 'TypeError', 'message': 'Type mismatch occurred'},
        {'type': 'KeyError', 'message': 'Missing required key'},
        {'type': 'AttributeError', 'message': 'Attribute not found'},
        {'type': 'RuntimeError', 'message': 'Runtime error occurred'}
    ]
    
    error_choice = random.choice(error_types)
    
    if error_count % 3 == 0:
        # Sometimes return error status codes
        return jsonify({
            'error': error_choice['message'],
            'type': error_choice['type'],
            'error_count': error_count
        }), 500
    else:
        # Sometimes raise exceptions
        raise Exception(f"{error_choice['type']}: {error_choice['message']}")

# MEMORY_PRESSURE - Memory leak simulation
@app.route('/memory-leak')
def memory_leak():
    global memory_leak_data
    
    # Simulate memory leak by storing large objects
    for _ in range(100):
        large_data = {
            'timestamp': datetime.now().isoformat(),
            'data': 'x' * 1000,  # 1KB per entry
            'random': random.random(),
            'list': list(range(100))
        }
        memory_leak_data.append(large_data)
    
    # Also create some large strings
    large_string = 'A' * 1000000  # 1MB string
    
    memory_info = psutil.virtual_memory()
    
    return jsonify({
        'message': 'Memory leak simulation',
        'leaked_objects': len(memory_leak_data),
        'memory_usage_percent': memory_info.percent,
        'available_memory_gb': memory_info.available / (1024**3)
    })

# CPU_PRESSURE - CPU intensive endpoint
@app.route('/cpu-intensive')
def cpu_intensive():
    start_time = time.time()
    
    # CPU intensive calculation
    def fibonacci(n):
        if n <= 1:
            return n
        return fibonacci(n-1) + fibonacci(n-2)
    
    # Calculate multiple fibonacci numbers
    results = []
    for i in range(30, 35):  # CPU intensive range
        result = fibonacci(i)
        results.append({'n': i, 'fibonacci': result})
    
    # Add more CPU work
    primes = []
    for num in range(2, 10000):
        is_prime = all(num % i != 0 for i in range(2, int(num**0.5) + 1))
        if is_prime:
            primes.append(num)
    
    cpu_percent = psutil.cpu_percent(interval=1)
    
    return jsonify({
        'message': 'CPU intensive task completed',
        'fibonacci_results': results,
        'primes_found': len(primes),
        'cpu_usage_percent': cpu_percent,
        'processing_time': time.time() - start_time
    })

# DATABASE_CONNECTION_ISSUES - Database timeout simulation
@app.route('/db-timeout')
def db_timeout():
    global error_count
    
    try:
        # Simulate connection timeout
        if random.random() < 0.3:  # 30% chance of timeout
            time.sleep(2)  # Simulate long query
            raise Exception("Connection timeout")
        
        # Simulate deadlock
        if random.random() < 0.2:  # 20% chance of deadlock
            raise Exception("Database deadlock detected")
        
        # Simulate too many connections
        if random.random() < 0.1:  # 10% chance
            raise Exception("Too many connections")
        
        conn = sqlite3.connect('app.db', timeout=1.0)  # Short timeout
        cursor = conn.cursor()
        
        # Simulate slow query
        time.sleep(random.uniform(0.1, 0.5))
        
        cursor.execute("SELECT COUNT(*) FROM users")
        count = cursor.fetchone()[0]
        conn.close()
        
        return jsonify({
            'message': 'Database operation successful',
            'user_count': count
        })
        
    except Exception as e:
        error_count += 1
        error_msg = str(e).lower()
        
        if 'timeout' in error_msg:
            return jsonify({'error': 'Connection timeout'}), 503
        elif 'deadlock' in error_msg:
            return jsonify({'error': 'Database deadlock'}), 503
        elif 'too many connections' in error_msg:
            return jsonify({'error': 'Too many connections'}), 503
        else:
            return jsonify({'error': f'SQL exception: {str(e)}'}), 500

# CACHE_PERFORMANCE_ISSUES - Cache simulation
@app.route('/cache-miss')
def cache_miss():
    # Simulate cache with poor performance
    cache_data = {}
    
    # Simulate cache miss (most of the time)
    if random.random() < 0.8:  # 80% miss rate
        # Simulate expensive computation
        time.sleep(0.1)  # Simulate slow backend call
        
        # Store in cache with very short TTL
        key = f"cache_key_{random.randint(1, 100)}"
        cache_data[key] = {
            'data': f'expensive_data_{random.randint(1, 1000)}',
            'timestamp': time.time()
        }
        
        return jsonify({
            'message': 'Cache miss - computed fresh data',
            'cache_hit': False,
            'data': cache_data[key]['data']
        })
    else:
        # Cache hit
        return jsonify({
            'message': 'Cache hit - retrieved from cache',
            'cache_hit': True,
            'data': 'cached_data'
        })

# CONNECTION_POOL_EXHAUSTION - Connection pool simulation
@app.route('/connection-pool')
def connection_pool():
    # Simulate connection pool exhaustion
    max_connections = 10
    active_connections = random.randint(8, 12)  # Sometimes exceeds max
    
    if active_connections >= max_connections:
        return jsonify({
            'error': 'Connection pool exhausted',
            'active_connections': active_connections,
            'max_connections': max_connections,
            'pool_usage_percent': (active_connections / max_connections) * 100
        }), 503
    else:
        # Simulate successful connection
        time.sleep(random.uniform(0.05, 0.2))  # Simulate connection time
        
        return jsonify({
            'message': 'Connection successful',
            'active_connections': active_connections,
            'max_connections': max_connections,
            'pool_usage_percent': (active_connections / max_connections) * 100
        })

# TIMEOUT_ISSUES - Timeout simulation
@app.route('/timeout')
def timeout_simulation():
    # Simulate various timeout scenarios
    timeout_type = random.choice(['read_timeout', 'connection_timeout', 'general_timeout'])
    
    if timeout_type == 'read_timeout':
        time.sleep(2)  # Simulate long read
        return jsonify({'error': 'Read timeout occurred'}), 504
    elif timeout_type == 'connection_timeout':
        time.sleep(1.5)  # Simulate connection timeout
        return jsonify({'error': 'Connection timeout'}), 504
    else:
        time.sleep(3)  # General timeout
        return jsonify({'error': 'Request timed out'}), 504

# THROUGHPUT_DEGRADATION - Slow response simulation
@app.route('/slow-throughput')
def slow_throughput():
    # Simulate throughput degradation
    time.sleep(random.uniform(0.5, 1.5))  # Variable delay
    
    return jsonify({
        'message': 'Response delayed due to throughput issues',
        'delay': time.time()
    })

# REPEATED_ERROR - Same error repeatedly
@app.route('/repeated-error')
def repeated_error():
    global error_count
    
    # Always return the same error
    error_message = "Database connection failed: Unable to establish connection to primary database"
    error_count += 1
    
    return jsonify({
        'error': error_message,
        'error_count': error_count,
        'timestamp': datetime.now().isoformat()
    }), 500

# STATUS_CODE_ISSUES - Various status codes
@app.route('/status-issues')
def status_issues():
    # Return various non-2xx status codes
    status_codes = [400, 401, 403, 404, 500, 502, 503]
    status = random.choice(status_codes)
    
    messages = {
        400: 'Bad Request - Invalid parameters',
        401: 'Unauthorized - Authentication required',
        403: 'Forbidden - Access denied',
        404: 'Not Found - Resource does not exist',
        500: 'Internal Server Error',
        502: 'Bad Gateway - Upstream service unavailable',
        503: 'Service Unavailable - Server overloaded'
    }
    
    return jsonify({
        'error': messages[status],
        'status_code': status
    }), status

# Health check endpoint
@app.route('/health')
def health():
    memory = psutil.virtual_memory()
    cpu = psutil.cpu_percent()
    
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat(),
        'memory_usage_percent': memory.percent,
        'cpu_usage_percent': cpu,
        'error_count': error_count,
        'avg_response_time': sum(request_times[-10:]) / len(request_times[-10:]) if request_times else 0
    })

if __name__ == '__main__':
    init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
