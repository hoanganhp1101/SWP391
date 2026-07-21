# SQL scripts

## File chính (chạy khi setup DB)

- [`../newdb.sql`](../newdb.sql) — **schema + seed MySQL đầy đủ**. Chỉ cần chạy file này để khởi tạo project mới.

### Tài khoản demo sau khi import `newdb.sql`

| Vai trò | Email | Mật khẩu (hash SHA2 trong DB) |
|---------|-------|-------------------------------|
| Bác sĩ | `bacsi@diabcare.vn` | `123456` |
| Bệnh nhân | `dothil@example.com` | `123456` |
| Quản trị | `admin@diabcare.vn` | `123456` |

> Dùng các email trên thay cho script cũ (`doctor@gmail.com`, `dothil_quangnam@example.com`).

## Script tùy chọn (không gộp vào newdb)

| File | Mục đích | Ghi chú |
|------|----------|---------|
| [`remove-nurse-role.sql`](remove-nurse-role.sql) | Migration một lần: xóa vai trò `y_ta` trên DB đang chạy | MySQL. **Không cần** nếu đã import lại `newdb.sql` |
| [`assign-patients-to-doctor.sql`](assign-patients-to-doctor.sql) | Gán bệnh nhân cho 1 bác sĩ test | **Legacy SQL Server** — không chạy trên MySQL |
| [`seed-health-records-test-data.sql`](seed-health-records-test-data.sql) | Seed hàng loạt `health_records` cho analytics | **Legacy SQL Server** |
| [`seed-alerts-test-data.sql`](seed-alerts-test-data.sql) | Seed alerts test | **Legacy SQL Server** |

## File đã gộp / loại bỏ

| File cũ | Trạng thái |
|---------|------------|
| `them.sql` | Đã gộp vào `newdb.sql` (master_foods, master_medications, patient_assignments, …) |
| `patient data.sql` | Đã gộp vào `newdb.sql` (seed bệnh nhân, health_records, …) |

> Project hiện dùng **MySQL** (`newdb.sql`). Các script SQL Server giữ lại chỉ để tham khảo hoặc môi trường cũ.
