/* ============================================================
   Gán bệnh nhân cho bác sĩ phụ trách (patients.bac_si_id)
   Dùng để test tính năng "bác sĩ chỉ xem bệnh nhân của mình".

   Bác sĩ test:
     email : doctor@gmail.com
     id    : 4b2e7463-bbf3-42cf-a594-0e15cce36811
   ============================================================ */

DECLARE @doctorId VARCHAR(36) = '4b2e7463-bbf3-42cf-a594-0e15cce36811';

-- 1) Kiểm tra trạng thái hiện tại của các bệnh nhân
SELECT id, user_id, bac_si_id
FROM patients;

-- 2) Gán TẤT CẢ bệnh nhân hiện có cho bác sĩ test (để có dữ liệu xem ngay)
UPDATE patients
SET bac_si_id = @doctorId;

/* --- Tùy chọn: chỉ gán những bệnh nhân chưa có bác sĩ ---
UPDATE patients
SET bac_si_id = @doctorId
WHERE bac_si_id IS NULL;
*/

/* --- Tùy chọn: chỉ gán 2 bệnh nhân đầu để kiểm tra việc lọc ẩn bớt ---
UPDATE patients
SET bac_si_id = @doctorId
WHERE id IN (SELECT TOP 2 id FROM patients ORDER BY id);
*/

-- 3) Kiểm tra lại sau khi gán
SELECT COUNT(*) AS so_benh_nhan_phu_trach
FROM patients
WHERE bac_si_id = @doctorId;

SELECT id, user_id, bac_si_id
FROM patients;
