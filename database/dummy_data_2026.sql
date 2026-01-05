-- ================================================================
-- DUMMY DATA FOR YEAR 2026
-- Insert sample orders and related data for testing dashboard
-- ================================================================

-- Insert orders for January 2026
INSERT INTO orders (id, order_id, user_id, address_id, subtotal, shipping_cost, total_price, payment_method, pickup_method, status_payment, status_order, created_at, updated_at)
VALUES 
-- Order 1 - January 2026 (PAID, COMPLETED)
('ord-2026-001', 'ORD-2026-001', 'user-cust-001', 'addr-cust1-home', 1500000, 25000, 1525000, 'TRANSFER', 'DELIVERY', 'PAID', 'COMPLETED', '2026-01-02 10:30:00', '2026-01-02 10:30:00'),

-- Order 2 - January 2026 (PAID, READY)
('ord-2026-002', 'ORD-2026-002', 'user-cust-002', 'addr-cust2-home', 2800000, 30000, 2830000, 'TRANSFER', 'DELIVERY', 'PAID', 'READY', '2026-01-03 14:20:00', '2026-01-03 14:20:00'),

-- Order 3 - January 2026 (PAID, PROCESSING)
('ord-2026-003', 'ORD-2026-003', 'user-cust-003', 'addr-cust3-home', 950000, 20000, 970000, 'TRANSFER', 'DELIVERY', 'PAID', 'PROCESSING', '2026-01-04 09:15:00', '2026-01-04 09:15:00'),

-- Order 4 - January 2026 (PAID, COMPLETED)
('ord-2026-004', 'ORD-2026-004', 'user-cust-004', 'addr-cust4-office', 3200000, 35000, 3235000, 'TRANSFER', 'DELIVERY', 'PAID', 'COMPLETED', '2026-01-05 11:45:00', '2026-01-05 11:45:00');

-- Insert order items for 2026 orders
INSERT INTO order_items (id, order_id, item_id, quantity, price_per_unit, subtotal)
VALUES 
-- Order 1 items
('oi-2026-001', 'ord-2026-001', 'item-prod-001', 3, 500000, 1500000),

-- Order 2 items
('oi-2026-002', 'ord-2026-002', 'item-prod-002', 4, 700000, 2800000),

-- Order 3 items
('oi-2026-003', 'ord-2026-003', 'item-prod-003', 2, 475000, 950000),

-- Order 4 items
('oi-2026-004-1', 'ord-2026-004', 'item-prod-004', 5, 400000, 2000000),
('oi-2026-004-2', 'ord-2026-004', 'item-prod-005', 4, 300000, 1200000);

-- Insert payments for 2026 orders
INSERT INTO payments (id, order_id, amount, payment_method, payment_status, payment_date, proof_url)
VALUES 
('pay-2026-001', 'ord-2026-001', 1525000, 'TRANSFER', 'APPROVED', '2026-01-02 10:35:00', '/uploads/payment-proofs/pay-2026-001.jpg'),
('pay-2026-002', 'ord-2026-002', 2830000, 'TRANSFER', 'APPROVED', '2026-01-03 14:25:00', '/uploads/payment-proofs/pay-2026-002.jpg'),
('pay-2026-003', 'ord-2026-003', 970000, 'TRANSFER', 'APPROVED', '2026-01-04 09:20:00', '/uploads/payment-proofs/pay-2026-003.jpg'),
('pay-2026-004', 'ord-2026-004', 3235000, 'TRANSFER', 'APPROVED', '2026-01-05 11:50:00', '/uploads/payment-proofs/pay-2026-004.jpg');

-- Insert shipments for 2026 orders
INSERT INTO shipments (id, order_id, address_id, courier_name, tracking_number, shipment_status, shipped_at, delivered_at, created_at)
VALUES 
('ship-2026-001', 'ord-2026-001', 'addr-cust1-home', 'JNE', 'JNE2026010201', 'DELIVERED', '2026-01-03 08:00:00', '2026-01-04 15:30:00', '2026-01-02 11:00:00'),
('ship-2026-002', 'ord-2026-002', 'addr-cust2-home', 'J&T', 'JT2026010301', 'SHIPPED', '2026-01-04 09:00:00', NULL, '2026-01-03 15:00:00'),
('ship-2026-003', 'ord-2026-003', 'addr-cust3-home', 'SiCepat', 'SC2026010401', 'PROCESSING', NULL, NULL, '2026-01-04 10:00:00'),
('ship-2026-004', 'ord-2026-004', 'addr-cust4-office', 'JNE', 'JNE2026010501', 'DELIVERED', '2026-01-05 13:00:00', '2026-01-05 18:00:00', '2026-01-05 12:00:00');

-- Summary
SELECT '========================================' AS '';
SELECT 'DUMMY DATA 2026 INSERTED SUCCESSFULLY!' AS status;
SELECT '========================================' AS '';
SELECT 'Summary:' AS info;
SELECT CONCAT('Total Orders 2026: ', COUNT(*)) as total_orders FROM orders WHERE YEAR(created_at) = 2026;
SELECT CONCAT('Total Units 2026: ', SUM(quantity)) as total_units FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE YEAR(o.created_at) = 2026;
SELECT CONCAT('Total Revenue 2026: Rp ', FORMAT(SUM(total_price), 0)) as total_revenue FROM orders WHERE status_payment = 'PAID' AND YEAR(created_at) = 2026;
