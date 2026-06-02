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
    CONSTRAINT fk_patients_user    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_patients_bac_si  FOREIGN KEY (bac_si_id) REFERENCES users(id) ON DELETE SET NULL
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
    ghi_chu             TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rx_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_rx_bac_si     FOREIGN KEY (bac_si_id)  REFERENCES users(id)    ON DELETE RESTRICT
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

-- ============================================================
-- 3. DỮ LIỆU MẪU (SAMPLE DATA)
-- ============================================================
-- Tạo biến lưu ID tĩnh để móc khóa ngoại (Foreign Keys) chính xác
SET @admin_id = UUID();
SET @doctor_id = UUID();
SET @nurse_id = UUID();
SET @patient_user_id = UUID();
SET @patient_profile_id = UUID();
SET @prescription_id = UUID();

-- 3.1 Nạp danh sách Users
INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash) VALUES
(@patient_user_id, 'Đỗ Thị L.', 'dothil_quangnam@example.com', '0981112233', 'benh_nhan', SHA2('password123', 256)),
(@doctor_id, 'Bác sĩ Trần Thị B', 'bacsi@example.com', '0912345678', 'bac_si', SHA2('doctor456', 256)),
(@nurse_id, 'Y tá Lê Văn C', 'yta@example.com', '0923456789', 'y_ta', SHA2('nurse789', 256)),
(@admin_id, 'Admin Hệ thống', 'admin@diabcare.vn', '0934567890', 'quan_tri_vien', SHA2('admin2024!', 256));

-- 3.2 Nạp Hồ sơ Bệnh nhân (Từ bệnh án)
INSERT INTO patients (id, user_id, bac_si_id, ngay_sinh, gioi_tinh, chieu_cao_cm, dia_chi, tien_su_benh, ngay_chan_doan_tieu_duong, loai_tieu_duong) 
VALUES (@patient_profile_id, @patient_user_id, @doctor_id, '1960-01-01', 'nu', 158.0, 'Núi Thành, Quảng Nam', 'ĐTĐ type 2 (3 năm), Tăng huyết áp', '2018-06-01', 'Type 2');

-- 3.3 Nạp Đơn thuốc của bác sĩ
INSERT INTO prescriptions (id, patient_id, bac_si_id, chan_doan, huong_dieu_tri) 
VALUES (@prescription_id, @patient_profile_id, @doctor_id, 'ĐTĐ type 2, RL Lipid máu', 'Khởi trị Insulin nền kết hợp Metformin.');

INSERT INTO medications (id, prescription_id, ten_thuoc, lieu_luong, don_vi, tan_suat) VALUES 
(UUID(), @prescription_id, 'Insulin lantus', '18', 'UI', '1 lần/ngày'),
(UUID(), @prescription_id, 'Metformin', '500', 'mg', '1 viên/ngày');

-- 3.4 Nạp dữ liệu đo sức khỏe 7 ngày (Glucose, Carbs, Insulin tracking)
INSERT INTO health_records (id, patient_id, duong_huyet_mgdl, thoi_diem_do_duong, lieu_luong_insulin_ui, loai_insulin_tiem, carbs_g, thoi_gian_do) VALUES 
(UUID(), @patient_profile_id, 210.5, 'luc_doi', 10, 'Insulin lantus', 180.0, DATE_SUB(CURDATE(), INTERVAL 7 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 195.0, 'luc_doi', 12, 'Insulin lantus', 170.0, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 180.2, 'luc_doi', 14, 'Insulin lantus', 165.0, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 165.5, 'luc_doi', 16, 'Insulin lantus', 150.0, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 145.0, 'luc_doi', 18, 'Insulin lantus', 145.0, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 125.5, 'luc_doi', 18, 'Insulin lantus', 140.0, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 110.0, 'luc_doi', 18, 'Insulin lantus', 135.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 7 HOUR),
(UUID(), @patient_profile_id, 100.0, 'luc_doi', 18, 'Insulin lantus', 135.0, CURDATE() + INTERVAL 7 HOUR);

-- 3.5 Nạp Lịch hẹn khám và Tài liệu y tế
INSERT INTO appointments (id, patient_id, bac_si_id, tieu_de, thoi_gian_hen, dia_diem, trang_thai) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'Tái khám Tim mạch', DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL '10:30' HOUR_MINUTE, 'Phòng khám St. Mary', 'cho_kham'),
(UUID(), @patient_profile_id, @doctor_id, 'Xét nghiệm máu tổng quát', DATE_ADD(CURDATE(), INTERVAL 12 DAY) + INTERVAL '08:00' HOUR_MINUTE, 'Trung tâm chẩn đoán', 'cho_kham');

INSERT INTO medical_documents (id, patient_id, bac_si_id, loai_tai_lieu, trang_thai, file_url, ngay_thuc_hien) VALUES
(UUID(), @patient_profile_id, @doctor_id, 'X-quang ngực', 'hoan_thanh', '#', DATE_SUB(CURDATE(), INTERVAL 10 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Xét nghiệm mỡ máu', 'can_xu_ly', '#', DATE_SUB(CURDATE(), INTERVAL 25 DAY)),
(UUID(), @patient_profile_id, @doctor_id, 'Tiêm chủng (Tdap)', 'hoan_thanh', '#', DATE_SUB(CURDATE(), INTERVAL 60 DAY));