import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VehicleDAO {
    public static void getAllVehicles() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String query = "SELECT * FROM vehicles";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                double rentalPrice = rs.getDouble("rental_price");
                String status = rs.getString("status");

                System.out.println(id + " | " + brand + " " + model + " | $" + rentalPrice + " | " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        getAllVehicles();
    }
}
