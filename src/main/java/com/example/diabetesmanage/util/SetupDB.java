package com.example.diabetesmanage.util;

import com.example.diabetesmanage.context.DBContext;

import java.sql.Connection;
import java.sql.Statement;

public class SetupDB {
    public static void main(String[] args) {
        String createDietPlans = "CREATE TABLE IF NOT EXISTS diet_plans (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "patient_id VARCHAR(50) NOT NULL, " +
                "doctor_id VARCHAR(50), " +
                "ngay_tao DATETIME NOT NULL, " +
                "ghi_chu TEXT" +
                ")";

        String createDietPlanDetails = "CREATE TABLE IF NOT EXISTS diet_plan_details (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "diet_plan_id VARCHAR(50) NOT NULL, " +
                "food_id VARCHAR(50) NOT NULL, " +
                "bua_an VARCHAR(50) NOT NULL, " +
                "ghi_chu TEXT, " +
                "FOREIGN KEY (diet_plan_id) REFERENCES diet_plans(id) ON DELETE CASCADE" +
                ")";

        String createMasterFoods = "CREATE TABLE IF NOT EXISTS master_foods (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "ten_thuc_pham VARCHAR(255) NOT NULL, " +
                "loai_mon VARCHAR(20), " +
                "don_vi_khau_phan VARCHAR(100), " +
                "carbs_g DOUBLE, " +
                "calo_kcal DOUBLE, " +
                "chi_so_gi DOUBLE, " +
                "trang_thai BOOLEAN DEFAULT TRUE, " +
                "ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DBContext.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createMasterFoods);
            System.out.println("Tạo bảng master_foods thành công!");

            stmt.execute(createDietPlans);
            System.out.println("Tạo bảng diet_plans thành công!");

            stmt.execute(createDietPlanDetails);
            System.out.println("Tạo bảng diet_plan_details thành công!");

            // Insert mock foods if empty or replace old mock foods
            stmt.execute("DELETE FROM master_foods WHERE id LIKE 'f%'");
            System.out.println("Đang cập nhật danh sách món ăn mẫu (xóa cũ, thêm mới)...");

            // Ghi chú: carbs/calo/GI dưới đây là số liệu ƯỚC TÍNH tham khảo
            // (đối chiếu Bảng chỉ số đường huyết quốc tế - Đại học Sydney,
            // USDA FoodData Central và ước lượng khẩu phần món Việt).
            // Không dùng thay thế tư vấn dinh dưỡng/y khoa chuyên môn cho bệnh nhân thật.
            String insertFood = "INSERT INTO master_foods (id, ten_thuc_pham, loai_mon, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES ";
            insertFood += "('f1', 'Phở bò chín', 'mon_chinh', '1 Bát vừa', 55, 430, 55, 1), ";
            insertFood += "('f2', 'Cơm gạo lứt', 'mon_chinh', '1 Bát con', 30, 150, 55, 1), ";
            insertFood += "('f3', 'Cá hồi áp chảo', 'mon_chinh', '1 Khúc (150g)', 0, 280, 0, 1), ";
            insertFood += "('f4', 'Súp lơ xanh luộc', 'rau_cu', '1 Đĩa con', 5, 30, 15, 1), ";
            insertFood += "('f5', 'Trứng ốp la', 'mon_phu', '1 Quả', 1, 90, 0, 1), ";
            insertFood += "('f6', 'Bánh mì đen nguyên cám', 'mon_chinh', '2 Lát', 24, 130, 55, 1), ";
            insertFood += "('f7', 'Ức gà luộc', 'mon_chinh', '100g', 0, 165, 0, 1), ";
            insertFood += "('f8', 'Salad dưa chuột cà chua', 'rau_cu', '1 Đĩa', 8, 45, 20, 1), ";
            insertFood += "('f9', 'Sữa chua không đường', 'mon_phu', '1 Hộp (100g)', 6, 60, 35, 1), ";
            insertFood += "('f10', 'Táo tây', 'trai_cay', '1 Quả vừa', 20, 80, 36, 1), ";
            // Các món thêm vào cho phong phú:
            insertFood += "('f11', 'Cháo yến mạch', 'mon_chinh', '1 Bát con', 27, 150, 60, 1), ";
            insertFood += "('f12', 'Khoai lang luộc', 'mon_chinh', '1 Củ vừa', 26, 112, 50, 1), ";
            insertFood += "('f13', 'Thịt bò xào măng tây', 'mon_chinh', '1 Đĩa vừa', 5, 220, 15, 1), ";
            insertFood += "('f14', 'Canh bí xanh nấu tôm', 'rau_cu', '1 Bát', 6, 70, 15, 1), ";
            insertFood += "('f15', 'Cá lóc kho tộ', 'mon_chinh', '1 Khúc', 8, 220, 35, 1), ";
            insertFood += "('f16', 'Đậu phụ sốt cà chua', 'mon_chinh', '1 Đĩa nhỏ', 10, 130, 30, 1), ";
            insertFood += "('f17', 'Sữa đậu nành không đường', 'mon_phu', '1 Ly (200ml)', 4, 80, 25, 1), ";
            insertFood += "('f18', 'Cam sành', 'trai_cay', '1 Quả vừa', 15, 60, 40, 1), ";
            insertFood += "('f19', 'Rau muống xào tỏi', 'rau_cu', '1 Đĩa', 6, 100, 15, 1), ";
            insertFood += "('f20', 'Bún lứt trộn ức gà', 'mon_chinh', '1 Bát', 45, 350, 50, 1), ";
            insertFood += "('f21', 'Nấm kim châm xào thịt băm', 'rau_cu', '1 Đĩa', 6, 170, 15, 1), ";
            insertFood += "('f22', 'Sinh tố bơ (không đường)', 'mon_phu', '1 Ly', 12, 230, 20, 1), ";
            insertFood += "('f23', 'Thịt nạc heo rang cháy cạnh', 'mon_chinh', '1 Đĩa nhỏ', 3, 270, 0, 1), ";
            insertFood += "('f24', 'Canh chua cá chép', 'mon_chinh', '1 Bát vừa', 12, 150, 35, 1), ";
            insertFood += "('f25', 'Ổi lai lê', 'trai_cay', '1 Quả', 14, 68, 24, 1), ";
            insertFood += "('f26', 'Măng tây hấp', 'rau_cu', '1 Đĩa', 4, 25, 15, 1), ";
            insertFood += "('f27', 'Gà nướng lá chanh', 'mon_chinh', '1 Đùi', 0, 210, 0, 1), ";
            insertFood += "('f28', 'Bún phở lứt chay', 'mon_chinh', '1 Bát', 50, 300, 50, 1), ";
            insertFood += "('f29', 'Cà rốt luộc', 'rau_cu', '1 Đĩa con', 6, 35, 39, 1), ";
            insertFood += "('f30', 'Sữa hạt điều', 'mon_phu', '1 Ly', 8, 120, 25, 1);";

            stmt.execute(insertFood);
            System.out.println("Đã thêm 30 món ăn mẫu phong phú!");

            System.out.println("==== HOÀN TẤT! BẠN CÓ THỂ ĐÓNG FILE NÀY VÀ QUAY LẠI TRÌNH DUYỆT ====");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}