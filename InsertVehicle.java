import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertVehicle {
    public static void insertVehicle(String brand, String model, double price, String status) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String sql = "INSERT INTO vehicles (brand, model, rental_price, status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, brand);
            pstmt.setString(2, model);
            pstmt.setDouble(3, price);
            pstmt.setString(4, status);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("車輛新增成功！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        insertVehicle("Toyota", "Camry", 3000, "可租");
    }
}
