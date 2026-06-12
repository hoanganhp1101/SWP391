-- ============================================================
-- PROJECT: DIABCARE AI - HỆ THỐNG QUẢN LÝ BỆNH ÁN TIỂU ĐƯỜNG
-- Mô tả: File khởi tạo cấu trúc CSDL và dữ liệu mẫu (Human-readable)
-- ============================================================

CREATE DATABASE IF NOT EXISTS diabcare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE diabcare_db;

-- ============================================================
-- 1. CẤU TRÚC BẢNG (TABLE SCHEMA)
-- ============================================================

CREATE TABLE users (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    ho_ten              VARCHAR(150)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    so_dien_thoai       VARCHAR(20)     DEFAULT NULL,
    vai_tro             ENUM('benh_nhan','bac_si','y_ta','quan_tri_vien') NOT NULL DEFAULT 'benh_nhan',
    mat_khau_hash       TEXT            NOT NULL,
    anh_dai_dien        TEXT            DEFAULT NULL,
    kich_hoat           TINYINT(1)      NOT NULL DEFAULT 1,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    lan_dang_nhap_cuoi  DATETIME        DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE patients (
                          id                          CHAR(36)        NOT NULL DEFAULT (UUID()),
                          user_id                     CHAR(36)        NOT NULL,
                          bac_si_id                   CHAR(36)        DEFAULT NULL,
                          ngay_sinh                   DATE            NOT NULL,
                          gioi_tinh                   ENUM('nam','nu','khac') DEFAULT NULL,
                          chieu_cao_cm                DECIMAL(5,1)    DEFAULT NULL,
                          can_nang_kg                 DECIMAL(5,1)    DEFAULT NULL, -- Trường bổ sung để tính BMI
                          dia_chi                     TEXT            DEFAULT NULL,
                          bao_hiem_y_te               VARCHAR(50)     DEFAULT NULL,
                          tien_su_benh                TEXT            DEFAULT NULL,
                          di_ung                      TEXT            DEFAULT NULL,
                          nhom_mau                    VARCHAR(5)      DEFAULT NULL,
                          ngay_chan_doan_tieu_duong   DATE            DEFAULT NULL,
                          loai_tieu_duong             VARCHAR(30)     DEFAULT NULL,
                          ngay_tao                    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          ngay_cap_nhat               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          CONSTRAINT fk_patients_user   FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_patients_bac_si FOREIGN KEY (bac_si_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=UTF8MB4_UNICODE_CI;


CREATE TABLE medical_encounters (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    bac_si_id           CHAR(36)        NOT NULL,
    ngay_kham           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ly_do_kham          TEXT            NOT NULL,
    qua_trinh_benh_ly   TEXT            DEFAULT NULL,
    kham_lam_sang       JSON            DEFAULT NULL COMMENT 'Lưu dữ liệu khám các cơ quan dưới dạng JSON',
    chan_doan_chinh     VARCHAR(255)    NOT NULL,
    chan_doan_phu       TEXT            DEFAULT NULL,
    huong_xu_tri        TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_enc_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_enc_bac_si  FOREIGN KEY (bac_si_id)  REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE lab_results (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    encounter_id        CHAR(36)        DEFAULT NULL,
    ngay_xet_nghiem     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Sinh hóa máu
    glucose_mau         DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    hba1c               DECIMAL(4,2)    DEFAULT NULL COMMENT '%',
    cholesterol_tp      DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    triglyceride        DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    hdl_c               DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    ldl_c               DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    ast                 DECIMAL(6,2)    DEFAULT NULL COMMENT 'U/L',
    alt                 DECIMAL(6,2)    DEFAULT NULL COMMENT 'U/L',
    ure                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'mmol/L',
    creatinine          DECIMAL(6,2)    DEFAULT NULL COMMENT 'umol/L',
    
    -- Miễn dịch
    hbsag               ENUM('am_tinh', 'duong_tinh') DEFAULT NULL,
    anti_hcv            ENUM('am_tinh', 'duong_tinh') DEFAULT NULL,
    
    -- Tổng phân tích tế bào máu
    wbc                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'K/uL',
    rbc                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'M/uL',
    hgb                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'g/dL',
    hct                 DECIMAL(5,2)    DEFAULT NULL COMMENT '%',
    plt                 DECIMAL(6,2)    DEFAULT NULL COMMENT 'K/uL',
    
    -- Nước tiểu (10 thông số cơ bản, lưu JSON để linh hoạt)
    nuoc_tieu           JSON            DEFAULT NULL,
    
    ghi_chu             TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_lab_patient   FOREIGN KEY (patient_id)   REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_lab_encounter FOREIGN KEY (encounter_id) REFERENCES medical_encounters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medication_logs (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    medication_id       CHAR(36)        NOT NULL, -- Link với thuốc trong đơn
    ngay_uong           DATE            NOT NULL, -- Cần uống vào ngày nào
    thoi_diem_du_kien   TIME            DEFAULT NULL, -- Giờ dự kiến (VD: 08:00 sáng)
    thoi_gian_thuc_te   DATETIME        DEFAULT NULL, -- Lúc bệnh nhân bấm tick
    trang_thai          ENUM('da_uong', 'bo_qua', 'chua_uong') NOT NULL DEFAULT 'chua_uong',
    ghi_chu             TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_ml_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_ml_med FOREIGN KEY (medication_id) REFERENCES medications(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE health_records (
    id                      CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id              CHAR(36)        NOT NULL,
    nhap_boi                CHAR(36)        DEFAULT NULL,
    duong_huyet_mgdl        DECIMAL(6,2)    DEFAULT NULL,
    thoi_diem_do_duong      ENUM('luc_doi','sau_an_1h','sau_an_2h','truoc_ngu') DEFAULT NULL,
    huyet_ap_tam_thu        SMALLINT        DEFAULT NULL,
    huyet_ap_tam_truong     SMALLINT        DEFAULT NULL,
    nhip_tim                SMALLINT        DEFAULT NULL,
    nhiet_do_c              DECIMAL(4,1)    DEFAULT NULL,
    nhip_tho                SMALLINT        DEFAULT NULL,
    can_nang_kg             DECIMAL(5,2)    DEFAULT NULL,
    bmi                     DECIMAL(4,2)    DEFAULT NULL,
    hba1c_percent           DECIMAL(4,2)    DEFAULT NULL,
    cholesterol_mmol        DECIMAL(5,2)    DEFAULT NULL,
    triglyceride_mmol       DECIMAL(5,2)    DEFAULT NULL,
    so_buoc_chan            INT             DEFAULT NULL,
    carbs_g                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'Lượng Carbohydrate nạp vào (gram)',
    so_gio_ngu              DECIMAL(3,1)    DEFAULT NULL,
    lieu_luong_insulin_ui   INT             DEFAULT NULL COMMENT 'Liều lượng insulin thực tế tiêm (UI)',
    loai_insulin_tiem       VARCHAR(100)    DEFAULT NULL COMMENT 'Loại insulin tiêm thực tế',
    ghi_chu                 TEXT            DEFAULT NULL,
    chest_pain              TINYINT(1)      DEFAULT 0,
    dizziness               TINYINT(1)      DEFAULT 0,
    fatigue                 TINYINT(1)      DEFAULT 0,
    thoi_gian_do            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_tao                DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hr_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_hr_nhap_boi   FOREIGN KEY (nhap_boi)   REFERENCES users(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE ai_analysis (
    id                      CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id              CHAR(36)        NOT NULL,
    health_record_id        CHAR(36)        DEFAULT NULL,
    diem_nguy_co            DECIMAL(5,2)    NOT NULL,
    muc_canh_bao            ENUM('an_toan','trung_binh','cao','nguy_hiem') NOT NULL,
    do_tin_cay              DECIMAL(4,3)    DEFAULT NULL,
    phan_tich_chi_tiet      TEXT            NOT NULL,
    yeu_to_nguy_co          JSON            DEFAULT NULL,
    khuyen_nghi             JSON            DEFAULT NULL,
    du_lieu_dau_vao         JSON            DEFAULT NULL,
    model_version           VARCHAR(50)     DEFAULT 'claude-sonnet-4-20250514',
    thoi_gian_phan_tich     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tokens_su_dung          INT             DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_patient    FOREIGN KEY (patient_id)       REFERENCES patients(id)      ON DELETE CASCADE,
    CONSTRAINT fk_ai_hr         FOREIGN KEY (health_record_id) REFERENCES health_records(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE alerts (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    ai_analysis_id      CHAR(36)        DEFAULT NULL,
    loai_canh_bao       ENUM('duong_huyet_cao','hba1c_bat_thuong','xu_huong_tang','khong_do_lien_tuc','bmi_cao') NOT NULL,
    muc_do              ENUM('an_toan','trung_binh','cao','nguy_hiem') NOT NULL,
    tieu_de             VARCHAR(200)    NOT NULL,
    noi_dung            TEXT            NOT NULL,
    da_doc_bn           TINYINT(1)      NOT NULL DEFAULT 0,
    da_doc_bs           TINYINT(1)      NOT NULL DEFAULT 0,
    xu_ly_boi           CHAR(36)        DEFAULT NULL,
    ghi_chu_xu_ly       TEXT            DEFAULT NULL,
    thoi_gian_tao       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_xu_ly     DATETIME        DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_alerts_patient    FOREIGN KEY (patient_id)     REFERENCES patients(id)    ON DELETE CASCADE,
    CONSTRAINT fk_alerts_ai         FOREIGN KEY (ai_analysis_id) REFERENCES ai_analysis(id) ON DELETE SET NULL,
    CONSTRAINT fk_alerts_xu_ly      FOREIGN KEY (xu_ly_boi)      REFERENCES users(id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE prescriptions (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    bac_si_id           CHAR(36)        NOT NULL,
    ngay_ke_don         DATE            NOT NULL DEFAULT (CURDATE()),
    chan_doan           TEXT            NOT NULL,
    huong_dieu_tri      TEXT            DEFAULT NULL,
    che_do_an           TEXT            DEFAULT NULL,
    luyen_tap           TEXT            DEFAULT NULL,
    ngay_tai_kham       DATETIME        DEFAULT NULL,
    encounter_id        CHAR(36)        DEFAULT NULL,
    ghi_chu             TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rx_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_rx_bac_si     FOREIGN KEY (bac_si_id)  REFERENCES users(id)    ON DELETE RESTRICT,
    CONSTRAINT fk_rx_encounter  FOREIGN KEY (encounter_id) REFERENCES medical_encounters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE medications (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    prescription_id     CHAR(36)        NOT NULL,
    ten_thuoc           VARCHAR(200)    NOT NULL,
    hoat_chat           VARCHAR(200)    DEFAULT NULL,
    lieu_luong          VARCHAR(100)    NOT NULL,
    don_vi              VARCHAR(50)     DEFAULT NULL,
    tan_suat            VARCHAR(100)    NOT NULL,
    thoi_diem_uong      VARCHAR(100)    DEFAULT NULL,
    thoi_gian_dung_ngay INT             DEFAULT NULL,
    ghi_chu             TEXT            DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_med_rx FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE notifications (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    alert_id        CHAR(36)        DEFAULT NULL,
    nguoi_nhan_id   CHAR(36)        NOT NULL,
    kenh_gui        ENUM('sms','email','push','in_app') NOT NULL,
    tieu_de         VARCHAR(200)    DEFAULT NULL,
    noi_dung        TEXT            NOT NULL,
    trang_thai      ENUM('cho_gui','da_gui','that_bai','da_doc') NOT NULL DEFAULT 'cho_gui',
    thoi_gian_gui   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoi_gian_doc   DATETIME        DEFAULT NULL,
    ma_loi          TEXT            DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_alert       FOREIGN KEY (alert_id)      REFERENCES alerts(id) ON DELETE SET NULL,
    CONSTRAINT fk_notif_nguoi_nhan  FOREIGN KEY (nguoi_nhan_id) REFERENCES users(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE appointments (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    bac_si_id           CHAR(36)        DEFAULT NULL,
    tieu_de             VARCHAR(200)    NOT NULL,
    thoi_gian_hen       DATETIME        NOT NULL,
    dia_diem            VARCHAR(255)    NOT NULL,
    trang_thai          ENUM('cho_kham','da_kham','da_huy') NOT NULL DEFAULT 'cho_kham',
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_app_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_bac_si  FOREIGN KEY (bac_si_id)  REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE medical_documents (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    bac_si_id           CHAR(36)        DEFAULT NULL,
    loai_tai_lieu       VARCHAR(200)    NOT NULL,
    trang_thai          ENUM('can_xu_ly','hoan_thanh','huy_bo') NOT NULL DEFAULT 'hoan_thanh',
    file_url            TEXT            DEFAULT NULL,
    ngay_thuc_hien      DATE            NOT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_doc_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_bac_si  FOREIGN KEY (bac_si_id)  REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE health_records
ADD COLUMN chest_pain TINYINT(1) DEFAULT 0,
ADD COLUMN dizziness TINYINT(1) DEFAULT 0,
ADD COLUMN fatigue TINYINT(1) DEFAULT 0;


-- ============================================================
-- 2. KHỞI TẠO VIEW TỔNG HỢP (Dùng cho Dashboard Bác sĩ)
-- ============================================================
CREATE OR REPLACE VIEW v_patient_summary AS
SELECT
    p.id                    AS patient_id,
    u.ho_ten,
    u.email,
    u.so_dien_thoai,
    p.ngay_sinh,
    TIMESTAMPDIFF(YEAR, p.ngay_sinh, CURDATE()) AS tuoi,
    p.gioi_tinh,
    p.loai_tieu_duong,
    hr.duong_huyet_mgdl     AS duong_huyet_gan_nhat,
    hr.bmi                  AS bmi_gan_nhat,
    hr.hba1c_percent        AS hba1c_gan_nhat,
    hr.thoi_gian_do         AS lan_do_cuoi,
    aa.muc_canh_bao         AS muc_nguy_co,
    aa.diem_nguy_co,
    (SELECT COUNT(*) FROM alerts al WHERE al.patient_id = p.id AND al.da_doc_bs = 0) AS canh_bao_chua_doc
FROM patients p
JOIN users u ON p.user_id = u.id
LEFT JOIN health_records hr ON hr.id = (
    SELECT id FROM health_records WHERE patient_id = p.id ORDER BY thoi_gian_do DESC LIMIT 1
)
LEFT JOIN ai_analysis aa ON aa.id = (
    SELECT id FROM ai_analysis WHERE patient_id = p.id ORDER BY thoi_gian_phan_tich DESC LIMIT 1
);

USE diabcare_db;

-- 1. Tắt tạm thời tính năng kiểm tra khóa ngoại (Rất quan trọng)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. Dọn sạch dữ liệu trong toàn bộ các bảng
TRUNCATE TABLE medical_documents;
TRUNCATE TABLE appointments;
TRUNCATE TABLE notifications;
TRUNCATE TABLE medications;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE alerts;
TRUNCATE TABLE ai_analysis;
TRUNCATE TABLE health_records;
TRUNCATE TABLE lab_results;
TRUNCATE TABLE medical_encounters;
TRUNCATE TABLE patients;
TRUNCATE TABLE users;

-- 3. Bật lại tính năng kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

USE diabcare_db;

-- ============================================================
-- 1. KHỞI TẠO BIẾN UUID ĐỂ LIÊN KẾT KHÓA NGOẠI CHÍNH XÁC
-- ============================================================
SET @admin_id = UUID();
SET @doctor_id = UUID();
SET @nurse_id = UUID();
SET @patient_user_id = UUID();
SET @patient_profile_id = UUID();
SET @prescription_id = UUID();

-- ============================================================
-- 2. TẠO TÀI KHOẢN NGƯỜI DÙNG (BÁC SĨ & BỆNH NHÂN)
-- ============================================================
INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash) VALUES
(@doctor_id, 'Bác sĩ Trần Thị B', 'bacsi@diabcare.vn', '0912345678', 'bac_si', SHA2('doctor123', 256)),
(@patient_user_id, 'Đỗ Thị L.', 'dothil@example.com', '0988777666', 'benh_nhan', SHA2('password123', 256)),
(@nurse_id, 'Y tá Lê Văn C', 'yta@example.com', '0923456789', 'y_ta', SHA2('nurse789', 256)),
(@admin_id, 'Admin Hệ thống', 'admin@diabcare.vn', '0934567890', 'quan_tri_vien', SHA2('admin2024!', 256));

-- ============================================================
-- 3. HỒ SƠ BỆNH NHÂN (Từ Bệnh án PDF)
-- ============================================================
INSERT INTO patients (id, user_id, bac_si_id, ngay_sinh, gioi_tinh, chieu_cao_cm, dia_chi, nghe_nghiep, tien_su_benh, ngay_chan_doan_tieu_duong, loai_tieu_duong) 
VALUES (
    @patient_profile_id, @patient_user_id, @doctor_id, '1960-01-01', 'nu', 158.0, 
    'Thị xã Núi Thành, Quảng Nam', 'Nội trợ', 
    'Đái tháo đường type 2 (3 năm), Tăng huyết áp (1 năm)', '2018-06-01', 'Type 2'
);

-- ============================================================
-- 4. LỊCH SỬ KHÁM BỆNH & CẬN LÂM SÀNG
-- ============================================================
INSERT INTO medical_encounters (id, patient_id, bac_si_id, ngay_kham, ly_do_kham, qua_trinh_benh_ly, chan_doan_chinh, chan_doan_phu, huong_xu_tri)
VALUES (
    @encounter_id, @patient_profile_id, @doctor_id, '2021-06-21 08:00:00', 
    'Mệt mỏi, tiểu nhiều', 
    'Cách nhập viện 2 tháng sụt 5kg, khát nhiều, tiểu nhiều.', 
    'Đái tháo đường type 2', 
    'Rối loạn lipid máu, Viêm gan cấp', 
    'Kiểm soát đường huyết bằng Insulin, ngưng dùng nước lá.'
);

INSERT INTO lab_results (patient_id, encounter_id, ngay_xet_nghiem, glucose_mau, hba1c, cholesterol_tp, triglyceride, hdl_c, ldl_c, ast, alt, ure, creatinine)
VALUES (@patient_profile_id, @encounter_id, '2021-06-25 14:00:00', 17.4, 12.2, 2.3, 2.3, 0.97, 1.29, 134, 85, 5.6, 67);

-- ============================================================
-- 5. ĐƠN THUỐC VÀ CHI TIẾT THUỐC (Từ Bệnh án PDF)
-- ============================================================
INSERT INTO prescriptions (id, patient_id, bac_si_id, encounter_id, chan_doan, huong_dieu_tri, che_do_an, luyen_tap) 
VALUES (
    @prescription_id, @patient_profile_id, @doctor_id, @encounter_id, 
    'ĐTĐ type 2, RL Lipid máu', 
    'Khởi trị Insulin nền kết hợp Metformin.', 
    'Hạn chế đồ ăn nhiều dầu mỡ, uống nhiều nước.', 
    'Tập thể dục đều đặn 30p/ngày.'
);

INSERT INTO medications (id, prescription_id, ten_thuoc, lieu_luong, don_vi, tan_suat) VALUES 
(UUID(), @prescription_id, 'Insulin Lantus', '18', 'UI', 'Tiêm dưới da 1 lần/ngày'),
(UUID(), @prescription_id, 'Metformin', '500', 'mg', '1 viên uống/ngày'),
(UUID(), @prescription_id, 'Rosuvas Hasan', '10', 'mg', '1 viên uống/ngày'),
(UUID(), @prescription_id, 'Sylimarin VCP', '140', 'mg', '2 viên uống/ngày'),
(UUID(), @prescription_id, 'Lorastad (Loratadine)', '10', 'mg', '1 viên uống/ngày');

-- ============================================================
-- 6. NHẬT KÝ SỨC KHỎE (Kết hợp nhập viện + chuỗi 7 ngày cho Biểu đồ)
-- ============================================================
-- Chỉ số nhập viện ban đầu (Đầy đủ Vitals)
INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, thoi_diem_do_duong, huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim, nhiet_do_c, nhip_tho, can_nang_kg, bmi, hba1c_percent, thoi_gian_do) 
VALUES (UUID(), @patient_profile_id, 313.2, 'luc_doi', 120, 80, 85, 37.0, 20, 61.0, 24.4, 12.2, '2021-06-21 08:00:00');

-- Chuỗi 7 ngày gần nhất để vẽ biểu đồ Dashboard
INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, thoi_diem_do_duong, lieu_luong_insulin_ui, loai_insulin_tiem, carbs_g, thoi_gian_do) VALUES 
(UUID(), @patient_profile_id, 210.5, 'luc_doi', 10, 'Insulin Lantus', 180.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 195.0, 'luc_doi', 12, 'Insulin Lantus', 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 180.2, 'luc_doi', 14, 'Insulin Lantus', 165.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 165.5, 'luc_doi', 16, 'Insulin Lantus', 150.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 145.0, 'luc_doi', 18, 'Insulin Lantus', 145.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 125.5, 'luc_doi', 18, 'Insulin Lantus', 140.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 110.0, 'luc_doi', 18, 'Insulin Lantus', 135.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 100.0, 'luc_doi', 18, 'Insulin Lantus', 135.0, CURDATE() + INTERVAL 7 HOUR);

-- ============================================================
-- 7. LỊCH HẸN KHÁM & TÀI LIỆU Y TẾ
-- ============================================================
INSERT INTO appointments (id, patient_id, bac_si_id, tieu_de, thoi_gian_hen, dia_diem, trang_thai) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'Tái khám Nội tiết', DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:30' HOUR_MINUTE, 'Phòng khám Đa khoa', 'cho_kham'),
(UUID(), @patient_profile_id, @doctor_id, 'Xét nghiệm máu tổng quát', DATE_ADD(CURDATE(), INTERVAL 12 DAY) + INTERVAL '08:00' HOUR_MINUTE, 'Khoa Xét nghiệm', 'cho_kham');

INSERT INTO medical_documents (id, patient_id, bac_si_id, loai_tai_lieu, trang_thai, file_url, ngay_thuc_hien) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'X-quang ngực thẳng', 'hoan_thanh', '#', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Siêu âm bụng', 'hoan_thanh', '#', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Kết quả sinh hóa máu', 'can_xu_ly', '#', DATE_SUB(CURDATE(), INTERVAL 25 DAY));