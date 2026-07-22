/* Chạy nhanh trên diabcare_db nếu cập nhật trạng thái báo thiếu trang_thai */
USE diabcare_db;
GO

IF COL_LENGTH('dbo.ai_analysis', 'trang_thai') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD trang_thai NVARCHAR(30) NOT NULL
        CONSTRAINT DF_ai_analysis_trang_thai DEFAULT N'chua_xem';
    PRINT N'Da them trang_thai';
END
ELSE PRINT N'trang_thai da co';
GO

IF COL_LENGTH('dbo.ai_analysis', 'ghi_chu_bs') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD ghi_chu_bs NVARCHAR(MAX) NULL;
    PRINT N'Da them ghi_chu_bs';
END
ELSE PRINT N'ghi_chu_bs da co';
GO

IF COL_LENGTH('dbo.ai_analysis', 'xu_ly_boi') IS NULL
BEGIN
    ALTER TABLE dbo.ai_analysis ADD xu_ly_boi UNIQUEIDENTIFIER NULL;
    PRINT N'Da them xu_ly_boi';
END
ELSE PRINT N'xu_ly_boi da co';
GO

SELECT name FROM sys.columns
WHERE object_id = OBJECT_ID(N'dbo.ai_analysis')
  AND name IN (N'trang_thai', N'ghi_chu_bs', N'xu_ly_boi');
GO
