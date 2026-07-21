-- ============================================================
-- Migration: Xóa vai trò Y tá (y_ta) khỏi DB đang chạy
-- Chạy khi KHÔNG muốn import lại toàn bộ newdb.sql
-- ============================================================

USE diabcare_db;

-- 1. Xóa tài khoản y tá (nếu còn)
DELETE FROM users WHERE vai_tro = 'y_ta' OR email = 'yta@example.com';

-- 2. Thu hẹp ENUM vai_tro (bắt buộc xóa hết y_ta trước)
ALTER TABLE users
    MODIFY COLUMN vai_tro ENUM('benh_nhan','bac_si','quan_tri_vien')
    NOT NULL DEFAULT 'benh_nhan';
