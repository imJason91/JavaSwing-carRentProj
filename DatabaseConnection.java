import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/CarRentalDB"; // MySQL 伺服器與資料庫名稱
    private static final String USER = "root"; // MySQL 帳號
    private static final String PASSWORD = "196203"; // MySQL 密碼

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 載入 MySQL JDBC 驅動
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 建立連線
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("資料庫連線成功！");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC 驅動未找到！");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("資料庫連線失敗！");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        // 測試連線
        getConnection();
    }
}
