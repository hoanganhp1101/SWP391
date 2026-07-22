/* ============================================================
   UC 17 — Bảng cấu hình ngưỡng giám sát (threshold_settings)
   Mỗi bác sĩ có một bộ ngưỡng riêng (bac_si_id UNIQUE).
   ============================================================ */

IF OBJECT_ID(N'dbo.threshold_settings', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.threshold_settings (
        id               UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        bac_si_id        UNIQUEIDENTIFIER NOT NULL,
        glucose_low      INT              NOT NULL DEFAULT 70,   -- hạ đường huyết / TIR min
        glucose_high     INT              NOT NULL DEFAULT 180,  -- cao / TIR max
        glucose_danger   INT              NOT NULL DEFAULT 250,  -- nguy hiểm (đỏ)
        hba1c_target     DECIMAL(4, 2)    NOT NULL DEFAULT 7.00,
        hba1c_poor       DECIMAL(4, 2)    NOT NULL DEFAULT 8.00,
        days_no_measure  INT              NOT NULL DEFAULT 7,
        ngay_cap_nhat    DATETIME2        NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_threshold_settings PRIMARY KEY (id),
        CONSTRAINT UQ_threshold_settings_bac_si UNIQUE (bac_si_id)
    );
    PRINT N'Đã tạo bảng threshold_settings.';
END
ELSE
    PRINT N'Bảng threshold_settings đã tồn tại.';

/* Seed mặc định cho bác sĩ test (tuỳ chọn — chạy khi chưa có dòng) */
DECLARE @doctorId UNIQUEIDENTIFIER = '4b2e7463-bbf3-42cf-a594-0e15cce36811';

IF NOT EXISTS (SELECT 1 FROM threshold_settings WHERE bac_si_id = @doctorId)
BEGIN
    INSERT INTO threshold_settings (id, bac_si_id, glucose_low, glucose_high, glucose_danger, hba1c_target, hba1c_poor, days_no_measure)
    VALUES (NEWID(), @doctorId, 70, 180, 250, 7.00, 8.00, 7);
    PRINT N'Đã seed ngưỡng mặc định cho bác sĩ test.';
END

SELECT * FROM threshold_settings;
