-- SQL Script để khởi tạo dữ liệu mẫu cho hệ thống đặt lịch cắt tóc

-- 1. Thêm Roles
INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_ADMIN'),
                                 (2, 'ROLE_BARBER'),
                                 (3, 'ROLE_CUSTOMER');

-- 2. Thêm Users (password đã được mã hóa BCrypt - password gốc là "123456")
INSERT INTO users (id, username, password, full_name, phone, role_id) VALUES
-- Admin
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Quản trị viên', '0900000000', 1),

-- Barbers
(2, 'minhtuan', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Minh Tuấn', '0901111111', 2),
(3, 'hoanglong', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Hoàng Long', '0902222222', 2),
(4, 'vanhai', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Văn Hải', '0903333333', 2),
(5, 'ducanh', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Đức Anh', '0904444444', 2),

-- Customers
(6, 'customer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Nguyễn Văn A', '0905555555', 3),
(7, 'customer2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', 'Trần Thị B', '0906666666', 3);

-- 3. Thêm Barbers
INSERT INTO barbers (id, experience, rating, user_id) VALUES
                                                          (1, '8 năm kinh nghiệm', 4.9, 2),
                                                          (2, '6 năm kinh nghiệm', 4.8, 3),
                                                          (3, '5 năm kinh nghiệm', 4.7, 4),
                                                          (4, '4 năm kinh nghiệm', 4.6, 5);

-- 4. Thêm Categories
INSERT INTO categories (id, name) VALUES
                                      (1, 'Cắt tóc'),
                                      (2, 'Uốn nhuộm'),
                                      (3, 'Massage'),
                                      (4, 'Combo');

-- 5. Thêm Services
INSERT INTO services (id, name, price, duration_min, category_id) VALUES
-- Cắt tóc
(1, 'Cắt tóc nam hiện đại', 150000, 30, 1),
(2, 'Cắt tóc + Gội đầu massage', 200000, 45, 1),
(3, 'Cắt tóc trẻ em', 100000, 25, 1),
(4, 'Cắt tóc nữ', 180000, 40, 1),

-- Uốn nhuộm
(5, 'Nhuộm tóc thời trang', 500000, 90, 2),
(6, 'Uốn tóc Hàn Quốc', 800000, 120, 2),
(7, 'Duỗi tóc', 600000, 100, 2),
(8, 'Ép tóc', 400000, 80, 2),

-- Massage
(9, 'Massage đầu thư giãn', 100000, 20, 3),
(10, 'Massage cổ vai gáy', 150000, 30, 3),

-- Combo
(11, 'Combo cắt + nhuộm', 600000, 120, 4),
(12, 'Combo cắt + uốn', 900000, 150, 4),
(13, 'Combo VIP (Cắt + Nhuộm + Massage)', 750000, 140, 4);

-- 6. Thêm Promotions
INSERT INTO promotions (id, code, discount_percent, expiry_date) VALUES
                                                                     (1, 'NEWBIE2026', 20, '2026-03-31'),
                                                                     (2, 'COMBO50', 50, '2026-02-28'),
                                                                     (3, 'HAPPYHOUR', 30, '2026-12-31'),
                                                                     (4, 'VIP100', 15, '2026-06-30'),
                                                                     (5, 'WEEKEND25', 25, '2026-04-30'),
                                                                     (6, 'NEWYEAR2026', 40, '2026-01-31');

-- 7. Thêm Appointments mẫu (cho customer1)
INSERT INTO appointments (id, booking_time, status, note, customer_id, barber_id, service_id) VALUES
                                                                                                  (1, '2026-01-15 10:00:00', 'CONFIRMED', 'Cắt ngắn hai bên', 6, 1, 1),
                                                                                                  (2, '2026-01-18 14:00:00', 'PENDING', '', 6, 2, 5),
                                                                                                  (3, '2026-01-10 09:00:00', 'COMPLETED', '', 6, 3, 2);

-- 8. Thêm Reviews mẫu
INSERT INTO reviews (id, star, comment, appointment_id) VALUES
    (1, 5, 'Dịch vụ tuyệt vời, thợ cắt tóc rất chuyên nghiệp và tận tâm!', 3);

-- 9. Thêm Work Shifts cho barbers
INSERT INTO work_shifts (id, date, start_time, end_time, barber_id) VALUES
-- Minh Tuấn (barber 1) - Thứ 2 đến Thứ 7
(1, '2026-01-12', '08:00:00', '17:00:00', 1),
(2, '2026-01-13', '08:00:00', '17:00:00', 1),
(3, '2026-01-14', '08:00:00', '17:00:00', 1),
(4, '2026-01-15', '08:00:00', '17:00:00', 1),
(5, '2026-01-16', '08:00:00', '17:00:00', 1),
(6, '2026-01-17', '08:00:00', '17:00:00', 1),

-- Hoàng Long (barber 2) - Thứ 2 đến Chủ nhật
(7, '2026-01-12', '09:00:00', '18:00:00', 2),
(8, '2026-01-13', '09:00:00', '18:00:00', 2),
(9, '2026-01-14', '09:00:00', '18:00:00', 2),
(10, '2026-01-15', '09:00:00', '18:00:00', 2),
(11, '2026-01-16', '09:00:00', '18:00:00', 2),
(12, '2026-01-17', '09:00:00', '18:00:00', 2),
(13, '2026-01-18', '09:00:00', '18:00:00', 2),

-- Văn Hải (barber 3) - Thứ 3 đến Chủ nhật
(14, '2026-01-13', '08:30:00', '17:30:00', 3),
(15, '2026-01-14', '08:30:00', '17:30:00', 3),
(16, '2026-01-15', '08:30:00', '17:30:00', 3),
(17, '2026-01-16', '08:30:00', '17:30:00', 3),
(18, '2026-01-17', '08:30:00', '17:30:00', 3),
(19, '2026-01-18', '08:30:00', '17:30:00', 3),

-- Đức Anh (barber 4) - Thứ 2 đến Thứ 6
(20, '2026-01-12', '09:00:00', '17:00:00', 4),
(21, '2026-01-13', '09:00:00', '17:00:00', 4),
(22, '2026-01-14', '09:00:00', '17:00:00', 4),
(23, '2026-01-15', '09:00:00', '17:00:00', 4),
(24, '2026-01-16', '09:00:00', '17:00:00', 4);

-- Reset auto increment counters (tùy theo database)
-- PostgreSQL
-- SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
-- SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
-- SELECT setval('barbers_id_seq', (SELECT MAX(id) FROM barbers));
-- SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
-- SELECT setval('services_id_seq', (SELECT MAX(id) FROM services));
-- SELECT setval('promotions_id_seq', (SELECT MAX(id) FROM promotions));
-- SELECT setval('appointments_id_seq', (SELECT MAX(id) FROM appointments));
-- SELECT setval('reviews_id_seq', (SELECT MAX(id) FROM reviews));
-- SELECT setval('work_shifts_id_seq', (SELECT MAX(id) FROM work_shifts));

-- MySQL
ALTER TABLE roles AUTO_INCREMENT = 4;
ALTER TABLE users AUTO_INCREMENT = 8;
ALTER TABLE barbers AUTO_INCREMENT = 5;
ALTER TABLE categories AUTO_INCREMENT = 5;
ALTER TABLE services AUTO_INCREMENT = 14;
ALTER TABLE promotions AUTO_INCREMENT = 7;
ALTER TABLE appointments AUTO_INCREMENT = 4;
ALTER TABLE reviews AUTO_INCREMENT = 2;
ALTER TABLE work_shifts AUTO_INCREMENT = 25;