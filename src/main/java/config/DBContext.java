    package config;

    import java.sql.Connection;
    import java.sql.DriverManager;

    public class DBContext {

        private static final String URL =
            "jdbc:sqlserver://localhost\\SQLEXPRESS:1433;databaseName=diabcare_db;encrypt=true;trustServerCertificate=true";

        private static final String USER = "sa";
        private static final String PASSWORD = "123";

        // ✅ Thêm biến connection để các DAO dùng trực tiếp
        protected Connection connection;

        // ✅ Constructor sẽ tự tạo connection
        public DBContext() {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                throw new RuntimeException("Database connection failed", e);
            }
        }

        // Vẫn giữ lại nếu sau này cần tạo connection mới
        public Connection getConnection() {
            return connection;
        }

        // Test connection
        public static void main(String[] args) {
            try {
                DBContext db = new DBContext();
                if (db.connection != null) {
                    System.out.println("✅ KẾT NỐI THÀNH CÔNG!");
                }
            } catch (Exception e) {
                System.out.println("❌ KẾT NỐI THẤT BẠI!");
                e.printStackTrace();
            }
        }
    }