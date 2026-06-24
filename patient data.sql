-- ============================================================
-- 1. TẠO TÀI KHOẢN (USER) CHO BỆNH NHÂN
-- ============================================================

USE diabcare_db;

-- Cột insulin đã có sẵn trong schema mới (newdb.sql), không ALTER lại ở đây để tránh lỗi cột không tồn tại.
SET @benh_nhan_email = 'dothil_quangnam@example.com';
SET @benh_nhan_user_id = (
    SELECT id
    FROM users
    WHERE email COLLATE utf8mb4_unicode_ci = @benh_nhan_email COLLATE utf8mb4_unicode_ci
    LIMIT 1
);
SET @benh_nhan_user_id = IFNULL(@benh_nhan_user_id, UUID());

INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash)
VALUES (
    @benh_nhan_user_id,
    'Đỗ Thị L.',
    @benh_nhan_email,
    '0981112233',
    'benh_nhan',
    SHA2('password123', 256)
)
ON DUPLICATE KEY UPDATE
    ho_ten = VALUES(ho_ten),
    so_dien_thoai = VALUES(so_dien_thoai),
    vai_tro = VALUES(vai_tro),
    mat_khau_hash = VALUES(mat_khau_hash);

-- ============================================================
-- 2. TẠO HỒ SƠ BỆNH NHÂN (PATIENT)
-- (Tự động gán cho Bác sĩ Trần Thị B từ Sample Data cũ)
-- ============================================================
SET @bac_si_phu_trach_id = (
    SELECT id
    FROM users
    WHERE vai_tro COLLATE utf8mb4_unicode_ci = 'bac_si' COLLATE utf8mb4_unicode_ci
    LIMIT 1
);

INSERT INTO users (id, ho_ten, email, so_dien_thoai, vai_tro, mat_khau_hash)
SELECT
    UUID(),
    'Bác sĩ mặc định',
    'bacsi_default@diabcare.vn',
    '0900000000',
    'bac_si',
    SHA2('doctor123', 256)
WHERE @bac_si_phu_trach_id IS NULL;

SET @bac_si_phu_trach_id = IFNULL(
    @bac_si_phu_trach_id,
    (SELECT id FROM users WHERE email = 'bacsi_default@diabcare.vn' LIMIT 1)
);

SET @patient_id = (
    SELECT id
    FROM patients
    WHERE user_id = @benh_nhan_user_id
    LIMIT 1
);

INSERT INTO patients (
    id, user_id, bac_si_id, ngay_sinh, gioi_tinh, chieu_cao_cm, 
    dia_chi, tien_su_benh, di_ung, ngay_chan_doan_tieu_duong, loai_tieu_duong
) SELECT
    UUID(), 
    @benh_nhan_user_id, 
    @bac_si_phu_trach_id, 
    '1960-01-01', 
    'nu', 
    158.0, 
    'Thị xã Núi Thành - huyện Núi Thành - Quảng Nam', 
    'ĐTĐ type 2 (3 năm); Tăng huyết áp (1 năm); Gia đình: Mẹ và chồng bị ĐTĐ type 2', 
    'Chưa ghi nhận tiền căn dị ứng thuốc, thức ăn', 
    '2018-06-01', 
    'Type 2'
WHERE @patient_id IS NULL;

SET @patient_id = IFNULL(
    @patient_id,
    (SELECT id FROM patients WHERE user_id = @benh_nhan_user_id LIMIT 1)
);

-- ============================================================
-- 3. CẬP NHẬT CHỈ SỐ SỨC KHỎE (HEALTH RECORD) LÚC NHẬP VIỆN
-- ============================================================
INSERT INTO health_records (
    id, patient_id, duong_huyet_mgdl, thoi_diem_do_duong, 
    huyet_ap_tam_thu, huyet_ap_tam_truong, nhip_tim, can_nang_kg, bmi, 
    hba1c_percent, cholesterol_mmol, triglyceride_mmol, ghi_chu, thoi_gian_do
) VALUES (
    UUID(), 
    @patient_id, 
    313.2, 
    'luc_doi', 
    120, 
    80, 
    85, 
    61.0, 
    24.4, 
    12.2, 
    2.3, 
    2.3, 
    'Bệnh nhân mệt mỏi, khát nhiều, tiểu nhiều, sụt 5kg/2 tháng. Da khô, ngứa toàn thân.', 
    '2021-06-25 08:00:00'
);

-- ============================================================
-- 4. TẠO ĐƠN THUỐC VÀ HƯỚNG ĐIỀU TRỊ (PRESCRIPTION)
-- ============================================================
SET @prescription_id = UUID();

INSERT INTO prescriptions (
    id, patient_id, bac_si_id, ngay_ke_don, chan_doan, 
    huong_dieu_tri, che_do_an, luyen_tap, ghi_chu
) VALUES (
    @prescription_id, 
    @patient_id, 
    @bac_si_phu_trach_id, 
    '2021-06-25', 
    'Đái tháo đường type 2, biến chứng rối loạn lipid máu - Viêm gan cấp do dùng thức uống không rõ loại.', 
    'Khởi trị Insulin nền kết hợp Metformin. Dự phòng tim mạch bằng statin. Điều trị triệu chứng ngứa và hỗ trợ gan.', 
    'Hạn chế đồ ăn nhiều dầu mỡ, rượu bia, uống nhiều nước, bổ sung vitamin thiết yếu', 
    'Tập thể dục đều đặn ít nhất 30p/ngày, 5 ngày/tuần. Mục tiêu đạt cân nặng 54kg', 
    'Tuyệt đối ngưng sử dụng các loại nước lá không rõ nguồn gốc.'
);

-- ============================================================
-- 5. KÊ CHI TIẾT CÁC LOẠI THUỐC (MEDICATIONS)
-- ============================================================
INSERT INTO medications (
    id, prescription_id, ten_thuoc, hoat_chat, lieu_luong, don_vi, tan_suat, thoi_diem_uong
) VALUES 
(UUID(), @prescription_id, 'Insulin lantus', 'Insulin glargine', '18', 'UI', '1 lần/ngày', 'Tiêm dưới da'),
(UUID(), @prescription_id, 'Metformin', 'Metformin', '500', 'mg', '1 viên/ngày', 'Uống'),
(UUID(), @prescription_id, 'Rosuvas Hasan', 'Rosuvastatin', '10', 'mg', '1 viên/ngày', 'Uống'),
(UUID(), @prescription_id, 'Sylimarin VCP', 'Silymarin', '140', 'mg', '2 viên/ngày', 'Uống'),
(UUID(), @prescription_id, 'Lorastad', 'Loratadine', '10', 'mg', '1 viên/ngày', 'Uống');