-- Sample data for the e-commerce demo application

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, category, created_at, updated_at) VALUES
('Laptop Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD', 1299.99, 50, 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 29.99, 200, 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 89.99, 75, 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4K Monitor', '27-inch 4K UHD monitor with HDR support', 399.99, 30, 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USB-C Hub', '7-in-1 USB-C hub with HDMI, USB 3.0, and SD card reader', 49.99, 150, 'Electronics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Office Chair', 'Ergonomic office chair with lumbar support', 249.99, 25, 'Furniture', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Standing Desk', 'Adjustable height standing desk with memory presets', 599.99, 15, 'Furniture', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 34.99, 100, 'Furniture', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Coffee Maker', 'Programmable coffee maker with thermal carafe', 79.99, 40, 'Appliances', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Water Bottle', 'Insulated stainless steel water bottle, 32oz', 19.99, 300, 'Accessories', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert some sample orders (for testing)
INSERT INTO orders (order_number, customer_email, total_amount, status, created_at, updated_at) VALUES
('ORD-001', 'john.doe@example.com', 1389.98, 'CONFIRMED', CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
('ORD-002', 'jane.smith@example.com', 119.98, 'SHIPPED', CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),
('ORD-003', 'bob.wilson@example.com', 849.97, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '3' HOUR, CURRENT_TIMESTAMP - INTERVAL '3' HOUR);

-- Insert order items for sample orders
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 1, 1299.99, 1299.99),
(1, 2, 3, 29.99, 89.99),
(2, 3, 1, 89.99, 89.99),
(2, 4, 1, 29.99, 29.99),
(3, 5, 1, 399.99, 399.99),
(3, 6, 1, 449.98, 449.98);
