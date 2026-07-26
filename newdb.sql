-- ============================================================
-- PROJECT: DIABCARE — HỆ THỐNG QUẢN LÝ BỆNH ÁN TIỂU ĐƯỜNG
-- NGUỒN CHÍNH (single source of truth) cho MySQL schema + seed.
-- Chỉ cần chạy file này khi setup / reset DB:
--   mysql -u root -p < newdb.sql
-- Các file SQL khác trong /scripts chỉ là migration/legacy tùy chọn.
-- ============================================================

CREATE DATABASE IF NOT EXISTS diabcare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE diabcare_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP VIEW IF EXISTS v_patient_summary;
DROP TABLE IF EXISTS educational_contents;
DROP TABLE IF EXISTS diet_plan_details;
DROP TABLE IF EXISTS diet_plans;
DROP TABLE IF EXISTS patient_assignments;
DROP TABLE IF EXISTS prescription_details;
DROP TABLE IF EXISTS medical_documents;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS medication_logs;
DROP TABLE IF EXISTS medications;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS alerts;
DROP TABLE IF EXISTS ai_analysis;
DROP TABLE IF EXISTS health_records;
DROP TABLE IF EXISTS lab_results;
DROP TABLE IF EXISTS medical_encounters;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS master_medications;
DROP TABLE IF EXISTS master_foods;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. CẤU TRÚC BẢNG (TABLE SCHEMA)
-- ============================================================

CREATE TABLE users (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    ho_ten              VARCHAR(150)    NOT NULL,
    email               VARCHAR(255)    NOT NULL,
    so_dien_thoai       VARCHAR(20)     DEFAULT NULL,
    vai_tro             ENUM('benh_nhan','bac_si','quan_tri_vien') NOT NULL DEFAULT 'benh_nhan',
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
                          patient_code                VARCHAR(32)     DEFAULT NULL,
                          user_id                     CHAR(36)        NOT NULL,
                          bac_si_id                   CHAR(36)        DEFAULT NULL,
                          ngay_sinh                   DATE            NOT NULL,
                          gioi_tinh                   ENUM('nam','nu','khac') DEFAULT NULL,
                          chieu_cao_cm                DECIMAL(5,1)    DEFAULT NULL,
                          can_nang_kg                 DECIMAL(5,1)    DEFAULT NULL, -- Trường bổ sung để tính BMI
                          dia_chi                     TEXT            DEFAULT NULL,
                          nghe_nghiep                 VARCHAR(100)    DEFAULT NULL,
                          bao_hiem_y_te               VARCHAR(50)     DEFAULT NULL,
                          tien_su_benh                TEXT            DEFAULT NULL,
                          tien_su_gia_dinh            TEXT            DEFAULT NULL,
                          di_ung                      TEXT            DEFAULT NULL,
                          nhom_mau                    VARCHAR(5)      DEFAULT NULL,
                          ngay_chan_doan_tieu_duong   DATE            DEFAULT NULL,
                          loai_tieu_duong             VARCHAR(30)     DEFAULT NULL,
                          ngay_tao                    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          ngay_cap_nhat               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (id),
                          UNIQUE KEY uq_patients_code (patient_code),
                          CONSTRAINT fk_patients_user   FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_patients_bac_si FOREIGN KEY (bac_si_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=UTF8MB4_UNICODE_CI;


CREATE TABLE medical_encounters (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    encounter_code      VARCHAR(32)     DEFAULT NULL,
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
    lab_result_code     VARCHAR(32)     DEFAULT NULL,
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

CREATE TABLE health_records (
    id                      CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id              CHAR(36)        NOT NULL,
    encounter_id            CHAR(36)        DEFAULT NULL,
    last_encounter_id       CHAR(36)        DEFAULT NULL,
    health_record_code      VARCHAR(32)     DEFAULT NULL,
    nhap_boi                CHAR(36)        DEFAULT NULL,
    chieu_cao_cm            DECIMAL(5,1)    DEFAULT NULL,
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
    hdl_mmol                DECIMAL(5,2)    DEFAULT NULL,
    ldl_mmol                DECIMAL(5,2)    DEFAULT NULL,
    wbc                     DECIMAL(5,2)    DEFAULT NULL,
    rbc                     DECIMAL(5,2)    DEFAULT NULL,
    hgb                     DECIMAL(5,2)    DEFAULT NULL,
    hct                     DECIMAL(5,2)    DEFAULT NULL,
    plt                     DECIMAL(6,2)    DEFAULT NULL,
    ast                     DECIMAL(6,2)    DEFAULT NULL,
    alt                     DECIMAL(6,2)    DEFAULT NULL,
    ure                     DECIMAL(5,2)    DEFAULT NULL,
    creatinine              DECIMAL(6,2)    DEFAULT NULL,
    so_buoc_chan            INT             DEFAULT NULL,
    carbs_g                 DECIMAL(5,2)    DEFAULT NULL COMMENT 'Lượng Carbohydrate nạp vào (gram)',
    so_gio_ngu              DECIMAL(3,1)    DEFAULT NULL,
    lieu_luong_insulin_ui   INT             DEFAULT NULL COMMENT 'Liều lượng insulin thực tế tiêm (UI)',
    loai_insulin_tiem       VARCHAR(100)    DEFAULT NULL COMMENT 'Loại insulin tiêm thực tế',
    trieu_chung             TEXT            DEFAULT NULL,
    tien_su_benh            TEXT            DEFAULT NULL,
    kham_lam_sang           JSON            DEFAULT NULL,
    chan_doan_chinh         VARCHAR(255)    DEFAULT NULL,
    chan_doan_phu           TEXT            DEFAULT NULL,
    phan_loai_tieu_duong    VARCHAR(50)     DEFAULT NULL,
    ghi_chu                 TEXT            DEFAULT NULL,
    chest_pain              TINYINT(1)      DEFAULT 0,
    dizziness               TINYINT(1)      DEFAULT 0,
    fatigue                 TINYINT(1)      DEFAULT 0,
    thoi_gian_do            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_tao                DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_hr_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_hr_nhap_boi   FOREIGN KEY (nhap_boi)   REFERENCES users(id)    ON DELETE SET NULL,
    CONSTRAINT fk_hr_encounter  FOREIGN KEY (encounter_id) REFERENCES medical_encounters(id) ON DELETE SET NULL
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
    INDEX idx_app_patient_time (patient_id, thoi_gian_hen),
    INDEX idx_app_patient_status_time (patient_id, trang_thai, thoi_gian_hen),
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
    INDEX idx_doc_patient_created (patient_id, ngay_tao, ngay_thuc_hien),
    CONSTRAINT fk_doc_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_bac_si  FOREIGN KEY (bac_si_id)  REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
                              loai_mon        VARCHAR(20),
                              don_vi_khau_phan VARCHAR(100)   NOT NULL COMMENT '100g, 1 bát, 1 quả...',
                              carbs_g         DECIMAL(5,2)    NOT NULL COMMENT 'Lượng Carbohydrate',
                              calo_kcal       DECIMAL(5,2)    DEFAULT NULL,
                              chi_so_gi       DECIMAL(5,2)    DEFAULT NULL COMMENT 'Chỉ số đường huyết (Glycemic Index)',
                              trang_thai      TINYINT(1)      NOT NULL DEFAULT 1,
                              ngay_tao        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Phân công bác sĩ điều trị
CREATE TABLE patient_assignments (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id      CHAR(36)        NOT NULL,
    doctor_id       CHAR(36)        NOT NULL,
    ngay_phan_cong  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    trang_thai      TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '1: Đang điều trị, 0: Đã dừng',
    PRIMARY KEY (id),
    INDEX idx_pa_patient_active (patient_id, trang_thai),
    CONSTRAINT fk_pa_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_doctor  FOREIGN KEY (doctor_id)  REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Thực đơn dinh dưỡng
CREATE TABLE diet_plans (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id      CHAR(36)        NOT NULL,
    doctor_id       VARCHAR(50)     DEFAULT NULL COMMENT 'user.id hoặc AI_SYSTEM',
    ngay_tao        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ghi_chu         TEXT            DEFAULT NULL,
    PRIMARY KEY (id),
    INDEX idx_diet_patient_created (patient_id, ngay_tao),
    CONSTRAINT fk_diet_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE diet_plan_details (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    diet_plan_id    CHAR(36)        NOT NULL,
    food_id         CHAR(36)        NOT NULL,
    bua_an          VARCHAR(50)     NOT NULL COMMENT 'Sáng, Trưa, Chiều, Tối, Phụ',
    ghi_chu         TEXT            DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dpd_plan FOREIGN KEY (diet_plan_id) REFERENCES diet_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_dpd_food FOREIGN KEY (food_id) REFERENCES master_foods(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Chi tiết đơn thuốc liên kết master_medications (PrescriptionDAO.createPrescription)
CREATE TABLE prescription_details (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    prescription_id     CHAR(36)        NOT NULL,
    medication_id       CHAR(36)        NOT NULL,
    lieu_luong          VARCHAR(100)    DEFAULT NULL,
    tan_suat            VARCHAR(100)    DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rxd_rx  FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE,
    CONSTRAINT fk_rxd_med FOREIGN KEY (medication_id)   REFERENCES master_medications(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nội dung giáo dục sức khỏe (admin educational content)
CREATE TABLE educational_contents (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    title               VARCHAR(200)    NOT NULL,
    category            VARCHAR(60)     NOT NULL,
    summary             VARCHAR(500)    DEFAULT NULL,
    content             TEXT            NOT NULL,
    target_audience     VARCHAR(60)     DEFAULT 'benh_nhan',
    display_order       INT             NOT NULL DEFAULT 0,
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_edu_category (category),
    INDEX idx_edu_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    hr.huyet_ap_tam_thu     AS huyet_ap_tam_thu,
    hr.huyet_ap_tam_truong  AS huyet_ap_tam_truong,
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
TRUNCATE TABLE educational_contents;
TRUNCATE TABLE diet_plan_details;
TRUNCATE TABLE diet_plans;
TRUNCATE TABLE patient_assignments;
TRUNCATE TABLE prescription_details;
TRUNCATE TABLE medical_documents;
TRUNCATE TABLE appointments;
TRUNCATE TABLE notifications;
TRUNCATE TABLE medication_logs;
TRUNCATE TABLE medications;
TRUNCATE TABLE prescriptions;
TRUNCATE TABLE alerts;
TRUNCATE TABLE ai_analysis;
TRUNCATE TABLE health_records;
TRUNCATE TABLE lab_results;
TRUNCATE TABLE medical_encounters;
TRUNCATE TABLE patients;
TRUNCATE TABLE master_medications;
TRUNCATE TABLE master_foods;
TRUNCATE TABLE users;

-- 3. Bật lại tính năng kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

USE diabcare_db;

-- ============================================================
-- 1. KHỞI TẠO BIẾN UUID ĐỂ LIÊN KẾT KHÓA NGOẠI CHÍNH XÁC
-- ============================================================
SET @admin_id = UUID();
SET @doctor_id = UUID();
SET @patient_user_id = UUID();
SET @patient_profile_id = UUID();
SET @encounter_id = UUID();
SET @prescription_id = UUID();

-- ============================================================
-- 2. TẠO TÀI KHOẢN NGƯỜI DÙNG (ADMIN, BÁC SĨ & BỆNH NHÂN)
-- ============================================================
INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash, anh_dai_dien) VALUES
(@doctor_id, 'Bác sĩ Trần Thị B', 'bacsi@diabcare.vn', '0912345678', 'bac_si', SHA2('123456', 256), 'https://ui-avatars.com/api/?name=Bac+Si+Tran+Thi+B&background=0D8ABC&color=fff'),
(@patient_user_id, 'Đỗ Thị L.', 'dothil@example.com', '0988777666', 'benh_nhan', SHA2('123456', 256), 'https://ui-avatars.com/api/?name=Do+Thi+L&background=0D8ABC&color=fff'),
(@admin_id, 'Admin Hệ thống', 'admin@diabcare.vn', '0934567890', 'quan_tri_vien', SHA2('123456', 256), 'https://ui-avatars.com/api/?name=Admin&background=1e293b&color=fff');

-- ============================================================
-- 3. HỒ SƠ BỆNH NHÂN (Từ Bệnh án PDF)
-- ============================================================
INSERT INTO patients (id, patient_code, user_id, bac_si_id, ngay_sinh, gioi_tinh, chieu_cao_cm, dia_chi, nghe_nghiep, tien_su_benh, ngay_chan_doan_tieu_duong, loai_tieu_duong) 
VALUES (
    @patient_profile_id, 'BN0001', @patient_user_id, @doctor_id, '1960-01-01', 'nu', 158.0, 
    'Thị xã Núi Thành, Quảng Nam', 'Nội trợ', 
    'Đái tháo đường type 2 (3 năm), Tăng huyết áp (1 năm)', '2018-06-01', 'Type 2'
);

-- ============================================================
-- 4. LỊCH SỬ KHÁM BỆNH & CẬN LÂM SÀNG
-- ============================================================
INSERT INTO medical_encounters (id, encounter_code, patient_id, bac_si_id, ngay_kham, ly_do_kham, qua_trinh_benh_ly, chan_doan_chinh, chan_doan_phu, huong_xu_tri)
VALUES (
    @encounter_id, 'ENC0001', @patient_profile_id, @doctor_id, '2021-06-21 08:00:00', 
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
-- Cần có đủ đường huyết, nhịp tim và huyết áp để biểu đồ hiển thị cả 4 đường.
INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, thoi_diem_do_duong, lieu_luong_insulin_ui, loai_insulin_tiem, carbs_g, nhip_tim, huyet_ap_tam_thu, huyet_ap_tam_truong, thoi_gian_do) VALUES 
(UUID(), @patient_profile_id, 210.5, 'luc_doi', 10, 'Insulin Lantus', 180.0, 88, 138, 86, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 195.0, 'luc_doi', 12, 'Insulin Lantus', 170.0, 86, 136, 84, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 180.2, 'luc_doi', 14, 'Insulin Lantus', 165.0, 84, 132, 82, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 165.5, 'luc_doi', 16, 'Insulin Lantus', 150.0, 82, 128, 80, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 145.0, 'luc_doi', 18, 'Insulin Lantus', 145.0, 80, 124, 78, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 125.5, 'luc_doi', 18, 'Insulin Lantus', 140.0, 78, 122, 76, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 110.0, 'luc_doi', 18, 'Insulin Lantus', 135.0, 76, 120, 75, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 100.0, 'luc_doi', 18, 'Insulin Lantus', 135.0, 74, 118, 74, CURDATE() + INTERVAL 7 HOUR);

-- ============================================================
-- 7. LỊCH HẸN KHÁM & TÀI LIỆU Y TẾ
-- ============================================================
INSERT INTO appointments (id, patient_id, bac_si_id, tieu_de, thoi_gian_hen, dia_diem, trang_thai, ngay_tao) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'Tái khám Nội tiết', DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:30' HOUR_MINUTE, 'Phòng khám Đa khoa', 'cho_kham', NOW()),
(UUID(), @patient_profile_id, @doctor_id, 'Xét nghiệm máu tổng quát', DATE_ADD(CURDATE(), INTERVAL 12 DAY) + INTERVAL '08:00' HOUR_MINUTE, 'Khoa Xét nghiệm', 'cho_kham', NOW()),
(UUID(), @patient_profile_id, @doctor_id, 'Khám kiểm tra biến chứng', DATE_SUB(CURDATE(), INTERVAL 20 DAY) + INTERVAL '09:00' HOUR_MINUTE, 'Phòng Nội tiết 02', 'da_kham', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Lịch tư vấn dinh dưỡng', DATE_SUB(CURDATE(), INTERVAL 35 DAY) + INTERVAL '14:30' HOUR_MINUTE, 'Phòng Tư vấn dinh dưỡng', 'da_huy', DATE_SUB(NOW(), INTERVAL 40 DAY));

INSERT INTO medical_documents (id, patient_id, bac_si_id, loai_tai_lieu, trang_thai, file_url, ngay_thuc_hien, ngay_tao) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'Bệnh án tái khám Nội tiết', 'hoan_thanh', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Kết quả xét nghiệm máu tổng quát', 'hoan_thanh', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Phiếu siêu âm bụng', 'hoan_thanh', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Kết quả sinh hóa máu', 'can_xu_ly', NULL, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 24 DAY));

-- ============================================================
-- 8. PHÂN CÔNG BÁC SĨ + TỪ ĐIỂN THUỐC / THỰC PHẨM (gộp từ them.sql)
-- ============================================================
INSERT INTO patient_assignments (id, patient_id, doctor_id, trang_thai)
VALUES (UUID(), @patient_profile_id, @doctor_id, 1);

INSERT INTO master_foods (id, ten_thuc_pham, loai_mon, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES
('f1', 'Cơm trắng', 'mon_chinh', '1 bát vừa (130g)', 36.5, 170.0, 73.0, 1),
('f2', 'Phở bò', 'mon_chinh', '1 bát', 60.0, 350.0, 65.0, 1),
('f3', 'Bánh mì thịt', 'mon_chinh', '1 ổ', 45.0, 380.0, 71.0, 1),
('f4', 'Táo tây', 'trai_cay', '1 quả vừa (150g)', 20.6, 78.0, 36.0, 1),
('f5', 'Sữa tươi không đường', 'mon_phu', '1 hộp (180ml)', 9.0, 110.0, 31.0, 1),
('f6', 'Cơm gạo lứt', 'mon_chinh', '1 bát con', 30.0, 150.0, 55.0, 1),
('f7', 'Ức gà luộc', 'mon_chinh', '100g', 0.0, 165.0, 0.0, 1),
('f8', 'Salad dưa chuột cà chua', 'rau_cu', '1 đĩa', 8.0, 45.0, 20.0, 1),
('f9', 'Sữa chua không đường', 'mon_phu', '1 hộp (100g)', 6.0, 60.0, 35.0, 1),
('f10', 'Rau muống xào tỏi', 'rau_cu', '1 đĩa', 6.0, 100.0, 15.0, 1);

INSERT INTO master_medications (id, ten_thuoc, hoat_chat, don_vi_tinh, loai_thuoc, huong_dan_goc, trang_thai) VALUES
('m1', 'Metformin 500mg', 'Metformin hydrochloride', 'Viên', 'Uống', 'Uống sau bữa ăn, không nhai hoặc nghiền nát.', 1),
('m2', 'Diamicron MR 30mg', 'Gliclazide', 'Viên', 'Uống', 'Uống 1 lần vào buổi sáng, ngay trước khi ăn.', 1),
('m3', 'Glucophage XR 750mg', 'Metformin', 'Viên', 'Uống', 'Uống vào buổi tối cùng với bữa ăn.', 1),
('m4', 'Lantus 100 IU/ml', 'Insulin Glargine', 'Bút tiêm', 'Tiêm dưới da', 'Tiêm 1 lần/ngày vào cùng một thời điểm. Luân phiên vị trí tiêm.', 1),
('m5', 'Novomix 30 FlexPen', 'Insulin Aspart', 'Bút tiêm', 'Tiêm dưới da', 'Tiêm ngay trước hoặc sau bữa ăn chính.', 1);

-- ============================================================
-- 9. NỘI DUNG GIÁO DỤC (gộp từ educational_contents.sql)
-- ============================================================
INSERT INTO educational_contents
    (id, title, category, summary, content, target_audience, display_order, active)
VALUES
    (UUID(), 'Nhận biết dấu hiệu hạ đường huyết', 'tu_cham_soc',
     'Các dấu hiệu cần chú ý và cách xử trí ban đầu khi đường huyết xuống thấp.',
     'Theo dõi các dấu hiệu như run tay, vã mồ hôi, chóng mặt, đói cồn cào hoặc lú lẫn. Khi nghi ngờ hạ đường huyết, người bệnh nên đo đường huyết nếu có máy đo và bổ sung carbohydrate hấp thu nhanh theo hướng dẫn của nhân viên y tế.',
     'benh_nhan', 1, 1),
    (UUID(), 'Nguyên tắc chọn thực phẩm GI thấp', 'dinh_duong',
     'Gợi ý lựa chọn thực phẩm giúp hạn chế tăng đường huyết sau ăn.',
     'Ưu tiên ngũ cốc nguyên hạt, rau xanh, đạm nạc và thực phẩm giàu chất xơ. Hạn chế nước ngọt, bánh kẹo, tinh bột tinh chế và khẩu phần quá lớn trong một bữa.',
     'benh_nhan', 2, 1),
    (UUID(), 'Lưu ý khi sử dụng insulin', 'thuoc_insulin',
     'Các điểm cần nhớ khi bảo quản và sử dụng insulin.',
     'Insulin cần được dùng đúng loại, đúng liều, đúng thời điểm theo chỉ định. Không tự ý thay đổi liều. Kiểm tra hạn dùng, cách bảo quản và vị trí tiêm để giảm nguy cơ sai liều hoặc kích ứng.',
     'benh_nhan', 3, 1);
