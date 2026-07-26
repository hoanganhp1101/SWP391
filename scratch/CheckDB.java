package scratch;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/diabcare_db?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh", "root", "123456");
            Statement stmt = conn.createStatement();
            
            System.out.println("--- diet_plan_details ---");
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT bua_an FROM diet_plan_details");
            while (rs.next()) {
                System.out.println("bua_an: '" + rs.getString("bua_an") + "'");
            }
            
            System.out.println("\n--- master_foods ---");
            ResultSet rs2 = stmt.executeQuery("SELECT id, ten_thuc_pham, carbs_g FROM master_foods LIMIT 5");
            while (rs2.next()) {
                System.out.println("id: '" + rs2.getString("id") + "', name: " + rs2.getString("ten_thuc_pham"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
