-- 1. Bảng Từ điển Thuốc
CREATE TABLE master_medications (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    ten_thuoc       VARCHAR(200)    NOT NULL,
    hoat_chat       VARCHAR(200)    DEFAULT NULL,
    don_vi_tinh     VARCHAR(50)     NOT NULL COMMENT 'Viên, Ống, Lọ, UI...',
    loai_thuoc      VARCHAR(100)    DEFAULT NULL COMMENT 'Insulin, Thuốc uống, Huyết áp...',
    huong_dan_goc   TEXT            DEFAULT NULL,
    trang_thai      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '1: Đang dùng, 0: Ngưng sử dụng',
    ngay_tao        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Từ điển Thực phẩm
CREATE TABLE master_foods (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    ten_thuc_pham   VARCHAR(200)    NOT NULL,
    don_vi_khau_phan VARCHAR(100)   NOT NULL COMMENT '100g, 1 bát, 1 quả...',
    carbs_g         DECIMAL(5,2)    NOT NULL COMMENT 'Lượng Carbohydrate',
    calo_kcal       DECIMAL(5,2)    DEFAULT NULL,
    chi_so_gi       DECIMAL(5,2)    DEFAULT NULL COMMENT 'Chỉ số đường huyết (Glycemic Index)',
    trang_thai      TINYINT(1)      NOT NULL DEFAULT 1,
    ngay_tao        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=UTF8MB4_UNICODE_CI;
-- Thêm dữ liệu mẫu cho bảng master_foods
INSERT INTO master_foods (id, ten_thuc_pham, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES 
(UUID(), 'Cơm trắng', '1 bát vừa (130g)', 36.5, 170.0, 73.0, true),
(UUID(), 'Phở bò', '1 bát', 60.0, 350.0, 65.0, true),
(UUID(), 'Bánh mì thịt', '1 ổ', 45.0, 380.0, 71.0, true),
(UUID(), 'Táo tây', '1 quả vừa (150g)', 20.6, 78.0, 36.0, true),
(UUID(), 'Sữa tươi không đường', '1 hộp (180ml)', 9.0, 110.0, 31.0, true);

-- Thêm dữ liệu mẫu cho bảng master_medications
INSERT INTO master_medications (id, ten_thuoc, hoat_chat, don_vi_tinh, loai_thuoc, huong_dan_goc, trang_thai) VALUES 
(UUID(), 'Metformin 500mg', 'Metformin hydrochloride', 'Viên', 'Uống', 'Uống sau bữa ăn, không nhai hoặc nghiền nát.', true),
(UUID(), 'Diamicron MR 30mg', 'Gliclazide', 'Viên', 'Uống', 'Uống 1 lần vào buổi sáng, ngay trước khi ăn.', true),
(UUID(), 'Glucophage XR 750mg', 'Metformin', 'Viên', 'Uống', 'Uống vào buổi tối cùng với bữa ăn.', true),
(UUID(), 'Lantus 100 IU/ml', 'Insulin Glargine', 'Bút tiêm', 'Tiêm dưới da', 'Tiêm 1 lần/ngày vào cùng một thời điểm. Luân phiên vị trí tiêm.', true),
(UUID(), 'Novomix 30 FlexPen', 'Insulin Aspart', 'Bút tiêm', 'Tiêm dưới da', 'Tiêm ngay trước hoặc sau bữa ăn chính.', true);

-- 1. Bảng phân công Bác sĩ điều trị cho Bệnh nhân
CREATE TABLE patient_assignments (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL, -- ID của người bệnh
    doctor_id VARCHAR(50) NOT NULL,  -- ID của Bác sĩ (hoặc Admin) đảm nhận
    ngay_phan_cong TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trang_thai BOOLEAN DEFAULT TRUE   -- True: Đang điều trị, False: Đã dừng
);
SELECT * FROM prescriptions
-- 2. Bảng Đơn thuốc (Tổng quan)
CREATE TABLE prescriptions (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL,
    doctor_id VARCHAR(50) NOT NULL,   -- Người kê đơn (Bác sĩ hoặc Admin)
    ngay_ke TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ghi_chu TEXT
);

-- 3. Bảng Chi tiết đơn thuốc (Liên kết với MasterMedication cũ)
CREATE TABLE prescription_details (
    id VARCHAR(50) PRIMARY KEY,
    prescription_id VARCHAR(50) NOT NULL,
    medication_id VARCHAR(50) NOT NULL, -- Khóa ngoại từ master_medications
    lieu_luong VARCHAR(100),            -- VD: 1 viên, 2 ống...
    tan_suat VARCHAR(100),              -- VD: Sáng 1 viên sau ăn, Tối 1 viên
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

-- 4. Bảng Đơn thực phẩm / Chế độ dinh dưỡng chỉ định (Tổng quan)
CREATE TABLE diet_plans (
    id VARCHAR(50) PRIMARY KEY,
    patient_id VARCHAR(50) NOT NULL,
    doctor_id VARCHAR(50) NOT NULL,   -- Người lên thực đơn
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ghi_chu TEXT
);

-- 5. Bảng Chi tiết thực đơn (Liên kết với MasterFood cũ)
CREATE TABLE diet_plan_details (
    id VARCHAR(50) PRIMARY KEY,
    diet_plan_id VARCHAR(50) NOT NULL,
    food_id VARCHAR(50) NOT NULL,       -- Khóa ngoại từ master_foods
    bua_an VARCHAR(50),                 -- VD: Sáng, Trưa, Chiều, Tối, Phụ
    ghi_chu TEXT,                       -- Ghi chú thêm (VD: Ăn bớt cơm lại)
    FOREIGN KEY (diet_plan_id) REFERENCES diet_plans(id) ON DELETE CASCADE
);