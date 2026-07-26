package scratch;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/diabcare_db?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh", "root", "123456");
            Statement stmt = conn.createStatement();
            
            try {
                stmt.execute("ALTER TABLE master_foods ADD COLUMN loai_mon VARCHAR(20)");
                System.out.println("Đã thêm cột loai_mon vào bảng master_foods.");
            } catch (Exception e) {
                System.out.println("Cột loai_mon có thể đã tồn tại.");
            }
            
            stmt.execute("DELETE FROM master_foods");
            
            String insertFood = "INSERT INTO master_foods (id, ten_thuc_pham, loai_mon, don_vi_khau_phan, carbs_g, calo_kcal, chi_so_gi, trang_thai) VALUES " +
            "('f1', 'Phở bò chín', 'mon_chinh', '1 Bát vừa', 55, 430, 55, 1), " +
            "('f2', 'Cơm gạo lứt', 'mon_chinh', '1 Bát con', 30, 150, 55, 1), " +
            "('f3', 'Cá hồi áp chảo', 'mon_chinh', '1 Khúc (150g)', 0, 280, 0, 1), " +
            "('f4', 'Súp lơ xanh luộc', 'rau_cu', '1 Đĩa con', 5, 30, 15, 1), " +
            "('f5', 'Trứng ốp la', 'mon_phu', '1 Quả', 1, 90, 0, 1), " +
            "('f6', 'Bánh mì đen nguyên cám', 'mon_chinh', '2 Lát', 24, 130, 55, 1), " +
            "('f7', 'Ức gà luộc', 'mon_chinh', '100g', 0, 165, 0, 1), " +
            "('f8', 'Salad dưa chuột cà chua', 'rau_cu', '1 Đĩa', 8, 45, 20, 1), " +
            "('f9', 'Sữa chua không đường', 'mon_phu', '1 Hộp (100g)', 6, 60, 35, 1), " +
            "('f10', 'Táo tây', 'trai_cay', '1 Quả vừa', 20, 80, 36, 1), " +
            "('f11', 'Cháo yến mạch', 'mon_chinh', '1 Bát con', 27, 150, 60, 1), " +
            "('f12', 'Khoai lang luộc', 'mon_chinh', '1 Củ vừa', 26, 112, 50, 1), " +
            "('f13', 'Thịt bò xào măng tây', 'mon_chinh', '1 Đĩa vừa', 5, 220, 15, 1), " +
            "('f14', 'Canh bí xanh nấu tôm', 'rau_cu', '1 Bát', 6, 70, 15, 1), " +
            "('f15', 'Cá lóc kho tộ', 'mon_chinh', '1 Khúc', 8, 220, 35, 1), " +
            "('f16', 'Đậu phụ sốt cà chua', 'mon_chinh', '1 Đĩa nhỏ', 10, 130, 30, 1), " +
            "('f17', 'Sữa đậu nành không đường', 'mon_phu', '1 Ly (200ml)', 4, 80, 25, 1), " +
            "('f18', 'Cam sành', 'trai_cay', '1 Quả vừa', 15, 60, 40, 1), " +
            "('f19', 'Rau muống xào tỏi', 'rau_cu', '1 Đĩa', 6, 100, 15, 1), " +
            "('f20', 'Bún lứt trộn ức gà', 'mon_chinh', '1 Bát', 45, 350, 50, 1), " +
            "('f21', 'Nấm kim châm xào thịt băm', 'rau_cu', '1 Đĩa', 6, 170, 15, 1), " +
            "('f22', 'Sinh tố bơ (không đường)', 'mon_phu', '1 Ly', 12, 230, 20, 1), " +
            "('f23', 'Thịt nạc heo rang cháy cạnh', 'mon_chinh', '1 Đĩa nhỏ', 3, 270, 0, 1), " +
            "('f24', 'Canh chua cá chép', 'mon_chinh', '1 Bát vừa', 12, 150, 35, 1), " +
            "('f25', 'Ổi lai lê', 'trai_cay', '1 Quả', 14, 68, 24, 1), " +
            "('f26', 'Măng tây hấp', 'rau_cu', '1 Đĩa', 4, 25, 15, 1), " +
            "('f27', 'Gà nướng lá chanh', 'mon_chinh', '1 Đùi', 0, 210, 0, 1), " +
            "('f28', 'Bún phở lứt chay', 'mon_chinh', '1 Bát', 50, 300, 50, 1), " +
            "('f29', 'Cà rốt luộc', 'rau_cu', '1 Đĩa con', 6, 35, 39, 1), " +
            "('f30', 'Sữa hạt điều', 'mon_phu', '1 Ly', 8, 120, 25, 1);";
            
            stmt.execute(insertFood);
            System.out.println("Đã thêm 30 món ăn với cột loai_mon thành công!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
