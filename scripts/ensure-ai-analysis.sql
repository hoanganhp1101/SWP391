/* ============================================================
   UC 16 — Bảng / cột cho AI Recommendations (ai_analysis)
   Chạy trên SQL Server (diabcare_db).
   ============================================================ */

-- Tạo bảng nếu chưa có
IF OBJECT_ID(N'dbo.ai_analysis', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ai_analysis (
        id                  UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
        patient_id          UNIQUEIDENTIFIER NOT NULL,
        health_record_id    UNIQUEIDENTIFIER NULL,
        diem_nguy_co        FLOAT            NULL,
        muc_canh_bao        NVARCHAR(50)     NULL,
        do_tin_cay          FLOAT            NULL,
        phan_tich_chi_tiet  NVARCHAR(MAX)    NULL,
        yeu_to_nguy_co      NVARCHAR(MAX)    NULL,
        khuyen_nghi         NVARCHAR(MAX)    NULL,
        du_lieu_dau_vao     NVARCHAR(MAX)    NULL,
        model_version       NVARCHAR(50)     NULL,
        thoi_gian_phan_tich DATETIME2        NOT NULL DEFAULT GETDATE(),
        tokens_su_dung      INT              NULL,
        trang_thai          NVARCHAR(30)     NOT NULL DEFAULT N'chua_xem',
        ghi_chu_bs          NVARCHAR(MAX)    NULL,
        xu_ly_boi           UNIQUEIDENTIFIER NULL,
        CONSTRAINT PK_ai_analysis PRIMARY KEY (id)
    );
    PRINT N'Đã tạo bảng ai_analysis.';
END
ELSE
    PRINT N'Bảng ai_analysis đã tồn tại.';

-- Thêm cột quản lý trạng thái nếu thiếu
IF COL_LENGTH('dbo.ai_analysis', 'trang_thai') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD trang_thai NVARCHAR(30) NOT NULL CONSTRAINT DF_ai_analysis_trang_thai DEFAULT N'chua_xem';
    PRINT N'Đã thêm cột trang_thai.';
END

IF COL_LENGTH('dbo.ai_analysis', 'ghi_chu_bs') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD ghi_chu_bs NVARCHAR(MAX) NULL;
    PRINT N'Đã thêm cột ghi_chu_bs.';
END

IF COL_LENGTH('dbo.ai_analysis', 'xu_ly_boi') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD xu_ly_boi UNIQUEIDENTIFIER NULL;
    PRINT N'Đã thêm cột xu_ly_boi.';
END

SELECT TOP 5 * FROM ai_analysis;

-- Xem cấu trúc cột thực tế (gửi kết quả này nếu insert vẫn lỗi)
SELECT c.name AS column_name, ty.name AS data_type, c.max_length, c.is_nullable
FROM sys.columns c
JOIN sys.types ty ON c.user_type_id = ty.user_type_id
WHERE c.object_id = OBJECT_ID(N'dbo.ai_analysis')
ORDER BY c.column_id;
