-- ============================================================
-- DiabCare AI — Database Schema (MySQL 8.0+)
-- Hệ thống Theo dõi Bệnh án & Cảnh báo Tiểu đường
-- ============================================================

CREATE DATABASE IF NOT EXISTS diabcare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE diabcare_db;

-- ============================================================
-- TABLE: users
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
    UNIQUE KEY uq_users_email (email),
    INDEX idx_users_vai_tro (vai_tro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: patients
-- ============================================================
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
    INDEX idx_patients_user_id (user_id),
    INDEX idx_patients_bac_si (bac_si_id),
    CONSTRAINT fk_patients_user    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_patients_bac_si  FOREIGN KEY (bac_si_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: health_records
-- ============================================================
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
    calo_tieu_thu           INT             DEFAULT NULL,
    so_gio_ngu              DECIMAL(3,1)    DEFAULT NULL,
    lieu_luong_insulin_ui   INT             DEFAULT NULL COMMENT 'Liều lượng insulin thực tế tiêm (đơn vị UI)',
    loai_insulin_tiem       VARCHAR(100)    DEFAULT NULL COMMENT 'Tên hoặc loại insulin tiêm thực tế (ví dụ: Lantus)',
    ghi_chu                 TEXT            DEFAULT NULL,
    thoi_gian_do            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_tao                DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_hr_patient        (patient_id),
    INDEX idx_hr_time           (thoi_gian_do DESC),
    INDEX idx_hr_patient_time   (patient_id, thoi_gian_do DESC),
    CONSTRAINT fk_hr_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_hr_nhap_boi   FOREIGN KEY (nhap_boi)   REFERENCES users(id)    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: ai_analysis
-- ============================================================
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
    INDEX idx_ai_patient    (patient_id),
    INDEX idx_ai_muc        (muc_canh_bao),
    INDEX idx_ai_time       (thoi_gian_phan_tich DESC),
    CONSTRAINT fk_ai_patient    FOREIGN KEY (patient_id)       REFERENCES patients(id)      ON DELETE CASCADE,
    CONSTRAINT fk_ai_hr         FOREIGN KEY (health_record_id) REFERENCES health_records(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: alerts
-- ============================================================
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
    INDEX idx_alerts_patient    (patient_id),
    INDEX idx_alerts_muc_do     (muc_do),
    INDEX idx_alerts_chua_doc   (da_doc_bs),
    CONSTRAINT fk_alerts_patient    FOREIGN KEY (patient_id)     REFERENCES patients(id)    ON DELETE CASCADE,
    CONSTRAINT fk_alerts_ai         FOREIGN KEY (ai_analysis_id) REFERENCES ai_analysis(id) ON DELETE SET NULL,
    CONSTRAINT fk_alerts_xu_ly      FOREIGN KEY (xu_ly_boi)      REFERENCES users(id)       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: prescriptions
-- ============================================================
CREATE TABLE prescriptions (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    patient_id          CHAR(36)        NOT NULL,
    bac_si_id           CHAR(36)        NOT NULL,
    ngay_ke_don         DATE            NOT NULL DEFAULT (CURDATE()),
    chan_doan            TEXT            NOT NULL,
    huong_dieu_tri      TEXT            DEFAULT NULL,
    che_do_an           TEXT            DEFAULT NULL,
    luyen_tap           TEXT            DEFAULT NULL,
    ngay_tai_kham       DATETIME        DEFAULT NULL,
    ghi_chu             TEXT            DEFAULT NULL,
    ngay_tao            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_rx_patient    (patient_id),
    INDEX idx_rx_bac_si     (bac_si_id),
    CONSTRAINT fk_rx_patient    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_rx_bac_si     FOREIGN KEY (bac_si_id)  REFERENCES users(id)    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: medications
-- ============================================================
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
    INDEX idx_med_prescription (prescription_id),
    CONSTRAINT fk_med_rx FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: notifications
-- ============================================================
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
    INDEX idx_notif_nguoi_nhan  (nguoi_nhan_id),
    INDEX idx_notif_trang_thai  (trang_thai),
    CONSTRAINT fk_notif_alert       FOREIGN KEY (alert_id)      REFERENCES alerts(id) ON DELETE SET NULL,
    CONSTRAINT fk_notif_nguoi_nhan  FOREIGN KEY (nguoi_nhan_id) REFERENCES users(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- VIEW: Tổng hợp bệnh nhân cho bác sĩ
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
    (
        SELECT COUNT(*) FROM alerts al
        WHERE al.patient_id = p.id AND al.da_doc_bs = 0
    )                       AS canh_bao_chua_doc
FROM patients p
JOIN users u ON p.user_id = u.id
LEFT JOIN health_records hr ON hr.id = (
    SELECT id FROM health_records
    WHERE patient_id = p.id
    ORDER BY thoi_gian_do DESC
    LIMIT 1
)
LEFT JOIN ai_analysis aa ON aa.id = (
    SELECT id FROM ai_analysis
    WHERE patient_id = p.id
    ORDER BY thoi_gian_phan_tich DESC
    LIMIT 1
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================
INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash) VALUES
(UUID(), 'Nguyễn Văn A',       'benhnhan@example.com', '0901234567', 'benh_nhan',    SHA2('password123', 256)),
(UUID(), 'Bác sĩ Trần Thị B',  'bacsi@example.com',    '0912345678', 'bac_si',       SHA2('doctor456',   256)),
(UUID(), 'Y tá Lê Văn C',      'yta@example.com',      '0923456789', 'y_ta',         SHA2('nurse789',    256)),
(UUID(), 'Admin Hệ thống',     'admin@diabcare.vn',    '0934567890', 'quan_tri_vien', SHA2('admin2024!', 256));
