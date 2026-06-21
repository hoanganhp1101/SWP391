-- ============================================================
-- Seed dữ liệu health_records cho màn /doctor/analytics (SQL Server)
-- 30 ngày x 4 lần đo/ngày cho MỖI bệnh nhân.
-- Dựa theo cấu trúc cột đã xác nhận chạy được.
-- ============================================================

USE [diabcare_db];
GO

-- ======================================================================
-- BƯỚC 1: Gỡ ràng buộc UNIQUE "code" gây lỗi (nếu vẫn còn)
-- ======================================================================
IF OBJECT_ID('UQ_health_records_code', 'UQ') IS NOT NULL
BEGIN
    ALTER TABLE health_records DROP CONSTRAINT UQ_health_records_code;
    PRINT N'Đã xóa ràng buộc UQ_health_records_code.';
END
GO

IF NOT EXISTS (SELECT 1 FROM patients)
BEGIN
    RAISERROR(N'Chưa có bệnh nhân trong bảng patients.', 16, 1);
    RETURN;
END
GO

-- ======================================================================
-- BƯỚC 2: Sinh dữ liệu 30 ngày x 4 thời điểm đo cho mỗi bệnh nhân
--   thoi_diem_do_duong chỉ dùng giá trị hợp lệ của CHECK constraint:
--   luc_doi, sau_an_1h, sau_an_2h, truoc_ngu
-- ======================================================================
;WITH days AS (
    SELECT TOP (30) ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) - 1 AS n
    FROM sys.all_objects
),
slots AS (
    SELECT 6  AS gio, CAST('luc_doi'   AS VARCHAR(20)) AS thoi_diem
    UNION ALL SELECT 9,  'sau_an_1h'
    UNION ALL SELECT 11, 'sau_an_2h'
    UNION ALL SELECT 21, 'truoc_ngu'
)
INSERT INTO health_records (
    id,
    patient_id,
    duong_huyet_mgdl,
    thoi_diem_do_duong,
    huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim,
    can_nang_kg, bmi, hba1c_percent,
    cholesterol_mmol, triglyceride_mmol,
    carbs_g, so_gio_ngu, lieu_luong_insulin_ui,
    thoi_gian_do
)
SELECT
    CAST(NEWID() AS CHAR(36)),
    CAST(p.id AS VARCHAR(36)),
    -- Đường huyết 60..259 mg/dL (có hạ đường huyết, trong ngưỡng, và cao)
    60 + ABS(CHECKSUM(NEWID())) % 200,
    s.thoi_diem,
    110 + ABS(CHECKSUM(NEWID())) % 40,      -- huyết áp tâm thu 110..149
    70  + ABS(CHECKSUM(NEWID())) % 20,      -- huyết áp tâm trương 70..89
    65  + ABS(CHECKSUM(NEWID())) % 30,      -- nhịp tim 65..94
    55  + (ABS(CHECKSUM(NEWID())) % 350) / 10.0,  -- cân nặng 55.0..89.9
    20  + (ABS(CHECKSUM(NEWID())) % 120) / 10.0,  -- BMI 20.0..31.9
    6.0 + (ABS(CHECKSUM(NEWID())) % 50)  / 10.0,  -- HbA1c 6.0..10.9 (đủ 3 nhóm)
    3.5 + (ABS(CHECKSUM(NEWID())) % 40)  / 10.0,  -- cholesterol 3.5..7.4
    1.0 + (ABS(CHECKSUM(NEWID())) % 30)  / 10.0,  -- triglyceride 1.0..3.9
    30  + ABS(CHECKSUM(NEWID())) % 120,     -- carbs 30..149 g
    5   + (ABS(CHECKSUM(NEWID())) % 40)  / 10.0,  -- số giờ ngủ 5.0..8.9
    ABS(CHECKSUM(NEWID())) % 20,            -- insulin 0..19 UI
    DATEADD(HOUR, s.gio, CAST(DATEADD(DAY, -d.n, CAST(GETDATE() AS DATE)) AS DATETIME))
FROM patients p
CROSS JOIN days d
CROSS JOIN slots s;

PRINT N'Đã seed dữ liệu health_records (30 ngày x 4 lần đo / bệnh nhân).';
GO

-- ======================================================================
-- BƯỚC 3: Kiểm tra nhanh các chỉ số của màn Analytics
-- ======================================================================
SELECT COUNT(*) AS tong_ban_ghi FROM health_records;

SELECT
    CAST(SUM(CASE WHEN duong_huyet_mgdl BETWEEN 70 AND 180 THEN 1 ELSE 0 END) AS FLOAT)
        * 100.0 / NULLIF(COUNT(*), 0)        AS time_in_range_pct,
    AVG(duong_huyet_mgdl)                    AS duong_huyet_tb,
    SUM(CASE WHEN duong_huyet_mgdl < 70 THEN 1 ELSE 0 END) AS so_lan_ha_duong,
    COUNT(DISTINCT CASE WHEN duong_huyet_mgdl < 70 THEN patient_id END) AS bn_ha_duong
FROM health_records
WHERE thoi_gian_do >= DATEADD(DAY, -30, GETDATE());

-- Phân nhóm HbA1c gần nhất theo từng bệnh nhân
;WITH latest AS (
    SELECT patient_id, hba1c_percent,
           ROW_NUMBER() OVER (PARTITION BY patient_id ORDER BY thoi_gian_do DESC) AS rn
    FROM health_records WHERE hba1c_percent IS NOT NULL
)
SELECT
    CASE WHEN hba1c_percent < 7 THEN N'Tốt (<7%)'
         WHEN hba1c_percent < 8 THEN N'Khá (7-8%)'
         ELSE N'Kém (>=8%)' END AS nhom,
    COUNT(*) AS so_benh_nhan
FROM latest WHERE rn = 1
GROUP BY CASE WHEN hba1c_percent < 7 THEN N'Tốt (<7%)'
              WHEN hba1c_percent < 8 THEN N'Khá (7-8%)'
              ELSE N'Kém (>=8%)' END;
