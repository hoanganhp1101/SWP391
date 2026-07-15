-- ============================================================
-- Seed dữ liệu test cho bảng alerts (SQL Server)
-- Chạy trên SSMS sau khi đã có bảng patients + users
-- ============================================================

USE [diabcare_db]; -- Đổi nếu DB của bạn khác tên
GO

-- ------------------------------------------------------------
-- (Tùy chọn) Xem các giá trị hợp lệ mà CHECK constraint cho phép.
-- Bỏ comment 2 dòng dưới và chạy riêng nếu insert vẫn lỗi.
-- ------------------------------------------------------------
-- SELECT name, definition FROM sys.check_constraints
-- WHERE parent_object_id = OBJECT_ID('dbo.alerts');
GO

-- Kiểm tra patient có sẵn
IF NOT EXISTS (SELECT 1 FROM patients)
BEGIN
    RAISERROR(N'Chưa có bệnh nhân trong bảng patients. Hãy tạo patient trước.', 16, 1);
    RETURN;
END
GO

DECLARE @p1 UNIQUEIDENTIFIER = (SELECT TOP 1 id FROM patients ORDER BY id);
DECLARE @p2 UNIQUEIDENTIFIER = (SELECT id FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM patients) t WHERE rn = 2);
DECLARE @p3 UNIQUEIDENTIFIER = (SELECT id FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn FROM patients) t WHERE rn = 3);

-- Nếu chỉ có 1 patient, dùng chung
SET @p2 = COALESCE(@p2, @p1);
SET @p3 = COALESCE(@p3, @p1);

-- LƯU Ý: loai_canh_bao chỉ dùng các giá trị đã có trong DB (qua CHECK constraint):
--   duong_huyet_cao, xu_huong_tang, khong_do_lien_tuc
INSERT INTO alerts
    (id, patient_id, ai_analysis_id, loai_canh_bao, muc_do, tieu_de, noi_dung,
     da_doc_bn, da_doc_bs, xu_ly_boi, ghi_chu_xu_ly, thoi_gian_tao, thoi_gian_xu_ly)
VALUES
-- Nguy hiểm (đỏ) — 9 bản ghi
(NEWID(), @p1, NULL, N'duong_huyet_cao',    N'nguy_hiem',   N'Đường huyết nguy hiểm',        N'Hệ thống ghi nhận đường huyết 320 mg/dL lúc 07:30.',                    0, 0, NULL, NULL,                         DATEADD(HOUR, -2,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'duong_huyet_cao',    N'nguy_hiem',   N'Đường huyết tăng đột biến',   N'Chỉ số vượt ngưỡng an toàn sau bữa sáng.',                              1, 0, NULL, NULL,                         DATEADD(HOUR, -5,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'nguy_hiem',   N'Không tiêm insulin 2 ngày',   N'Bệnh nhân không ghi nhận tiêm insulin từ 15/06.',                       0, 1, NULL, N'Đã gọi điện nhắc bệnh nhân', DATEADD(HOUR, -8,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'duong_huyet_cao',    N'nguy_hiem',   N'Hạ đường huyết nghiêm trọng', N'Đường huyết 45 mg/dL, cần can thiệp ngay.',                             0, 0, NULL, NULL,                         DATEADD(HOUR, -12, GETDATE()), NULL),
(NEWID(), @p2, NULL, N'xu_huong_tang',      N'nguy_hiem',   N'Xu hướng tăng bất thường',    N'Đường huyết tăng liên tục 5 ngày qua.',                                 1, 1, NULL, N'Đã chỉnh liều thuốc',        DATEADD(DAY,  -1,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'nguy_hiem',   N'Không đo chỉ số 72 giờ',      N'Bệnh nhân ngưng đo đường huyết hơn 3 ngày.',                            0, 0, NULL, NULL,                         DATEADD(DAY,  -1,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'duong_huyet_cao',    N'nguy_hiem',   N'Cảnh báo đỏ buổi tối',        N'Đường huyết 285 mg/dL trước khi ngủ.',                                  0, 1, NULL, NULL,                         DATEADD(DAY,  -2,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'khong_do_lien_tuc',  N'nguy_hiem',   N'Quên đo nhiều lần liên tục',  N'Không có log đo trong nhiều khung giờ hôm nay.',                        1, 0, NULL, NULL,                         DATEADD(DAY,  -2,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'xu_huong_tang',      N'nguy_hiem',   N'Tăng nhanh trong ngày',       N'Đường huyết tăng nhanh sau các bữa ăn.',                                0, 0, NULL, NULL,                         DATEADD(DAY,  -3,  GETDATE()), NULL),

-- Cao (vàng) — 9 bản ghi
(NEWID(), @p1, NULL, N'duong_huyet_cao',    N'cao',         N'Đường huyết cao',             N'Chỉ số 210 mg/dL sau ăn, cần theo dõi.',                                1, 1, NULL, NULL,                         DATEADD(HOUR, -3,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'xu_huong_tang',      N'cao',         N'Xu hướng đường huyết tăng',   N'Trung bình 7 ngày tăng 15%.',                                           0, 0, NULL, NULL,                         DATEADD(HOUR, -6,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'khong_do_lien_tuc',  N'cao',         N'Bỏ lỡ lịch đo sáng',          N'Bệnh nhân chưa đo chỉ số buổi sáng hôm nay.',                            0, 1, NULL, N'Đã nhắc đo chỉ số',          DATEADD(HOUR, -10, GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'cao',         N'Chưa xác nhận uống thuốc',    N'Chưa có log uống thuốc tối hôm qua.',                                   1, 0, NULL, NULL,                         DATEADD(HOUR, -14, GETDATE()), NULL),
(NEWID(), @p2, NULL, N'duong_huyet_cao',    N'cao',         N'Đường huyết sau ăn cao',      N'Postprandial 230 mg/dL.',                                               0, 0, NULL, NULL,                         DATEADD(DAY,  -1,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'xu_huong_tang',      N'cao',         N'HbA1c dự báo tăng',           N'Mô hình AI dự báo HbA1c vượt mục tiêu.',                                 1, 1, NULL, NULL,                         DATEADD(DAY,  -1,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'cao',         N'Quên đo buổi chiều',          N'Không có dữ liệu đo lúc 15:00.',                                        0, 0, NULL, NULL,                         DATEADD(DAY,  -3,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'duong_huyet_cao',    N'cao',         N'Đường huyết ranh ngưỡng',     N'Chỉ số 180 mg/dL, gần ngưỡng cao.',                                     1, 0, NULL, NULL,                         DATEADD(DAY,  -3,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'xu_huong_tang',      N'cao',         N'Dao động tăng dần',           N'Chỉ số dao động tăng dần trong tuần.',                                  0, 0, NULL, NULL,                         DATEADD(DAY,  -4,  GETDATE()), NULL),

-- Trung bình (xanh) — 10 bản ghi
(NEWID(), @p3, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'Bỏ lỡ 1 lần đo',             N'Bệnh nhân chưa đo chỉ số buổi trưa.',                                   1, 0, NULL, NULL,                         DATEADD(HOUR, -1,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'Nhắc nhở uống thuốc',        N'Chưa xác nhận uống thuốc sáng (có thể trễ).',                           0, 1, NULL, NULL,                         DATEADD(HOUR, -4,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'duong_huyet_cao',    N'trung_binh',  N'Đường huyết hơi cao',        N'Chỉ số 155 mg/dL, trong vùng theo dõi.',                                1, 1, NULL, NULL,                         DATEADD(HOUR, -7,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'xu_huong_tang',      N'trung_binh',  N'Biến động nhẹ',              N'Đường huyết dao động trong 3 ngày.',                                    0, 0, NULL, NULL,                         DATEADD(HOUR, -9,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'Chưa sync thiết bị',         N'Thiết bị đo chưa đồng bộ dữ liệu 6 giờ.',                               0, 0, NULL, NULL,                         DATEADD(HOUR, -11, GETDATE()), NULL),
(NEWID(), @p2, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'Uống thuốc trễ 30 phút',     N'Bệnh nhân xác nhận uống thuốc trễ.',                                     1, 0, NULL, NULL,                         DATEADD(DAY,  -1,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'duong_huyet_cao',    N'trung_binh',  N'Đường huyết ổn định cao',    N'Chỉ số 140 mg/dL, cần duy trì thói quen.',                              0, 1, NULL, N'Đã gọi điện nhắc bệnh nhân', DATEADD(DAY,  -2,  GETDATE()), NULL),
(NEWID(), @p1, NULL, N'xu_huong_tang',      N'trung_binh',  N'Xu hướng tăng nhẹ',          N'Tăng nhẹ so với tuần trước.',                                           1, 0, NULL, NULL,                         DATEADD(DAY,  -4,  GETDATE()), NULL),
(NEWID(), @p2, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'Nhắc đo cuối ngày',          N'Chưa có log đo trước 22:00.',                                           0, 0, NULL, NULL,                         DATEADD(DAY,  -5,  GETDATE()), NULL),
(NEWID(), @p3, NULL, N'duong_huyet_cao',    N'trung_binh',  N'Theo dõi sau ăn',            N'Chỉ số 150 mg/dL sau bữa trưa.',                                        0, 0, NULL, NULL,                         DATEADD(DAY,  -6,  GETDATE()), NULL),

-- Đã giải quyết (test trạng thái resolved) — 3 bản ghi
(NEWID(), @p1, NULL, N'duong_huyet_cao',    N'cao',         N'[Đã xử lý] Đường huyết cao',  N'Chỉ số đã về bình thường sau điều chỉnh.',                              1, 1, NULL, N'Đã chỉnh liều và theo dõi',  DATEADD(DAY,  -6,  GETDATE()), DATEADD(DAY, -5, GETDATE())),
(NEWID(), @p2, NULL, N'khong_do_lien_tuc',  N'trung_binh',  N'[Đã xử lý] Nhắc thuốc',      N'Bệnh nhân đã xác nhận uống thuốc đầy đủ.',                              1, 1, NULL, N'Đã gọi điện xác nhận',       DATEADD(DAY,  -7,  GETDATE()), DATEADD(DAY, -6, GETDATE())),
(NEWID(), @p3, NULL, N'khong_do_lien_tuc',  N'nguy_hiem',   N'[Đã xử lý] Không đo 48h',     N'Bệnh nhân đã đo lại và ổn định.',                                       1, 1, NULL, N'Đã nhắc và xác nhận đo',     DATEADD(DAY,  -8,  GETDATE()), DATEADD(DAY, -7, GETDATE()));

PRINT N'Đã thêm 31 cảnh báo test.';
GO

-- Kiểm tra nhanh sau khi insert
SELECT muc_do, COUNT(*) AS so_luong
FROM alerts
GROUP BY muc_do
ORDER BY muc_do;

SELECT TOP 15 id, patient_id, loai_canh_bao, muc_do, tieu_de, da_doc_bs, thoi_gian_tao
FROM alerts
ORDER BY thoi_gian_tao DESC;
