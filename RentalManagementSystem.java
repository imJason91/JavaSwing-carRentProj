import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class RentalManagementSystem {
    JFrame frame;
    JTable rentalTable;
    DefaultTableModel tableModel;
    Connection conn;

    public RentalManagementSystem() {
        conn = DatabaseConnection.getConnection();
        frame = new JFrame("租賃管理");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 設置表格模型
        String[] columnNames = {"編號", "車輛型號", "客戶", "開始日期", "結束日期", "租金"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // 初始化表格
        rentalTable = new JTable(tableModel);
        rentalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(rentalTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 加載資料庫
        loadRentalsFromDatabase();

        // 控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton startRentalButton = new JButton("開始租賃");
        startRentalButton.addActionListener(e -> startRental());

        JButton updateButton = new JButton("修改租賃");
        updateButton.addActionListener(e -> updateRental());

        JButton deleteButton = new JButton("刪除租賃");
        deleteButton.addActionListener(e -> deleteRental());

        JButton filterButton = new JButton("篩選");
        filterButton.addActionListener(e -> filterRentals());

        JButton returnVehicleButton = new JButton("歸還車輛");
        returnVehicleButton.addActionListener(e -> returnVehicle());

        JButton returnButton = new JButton("返回主頁");
        returnButton.addActionListener(e -> frame.dispose());

        controlPanel.add(startRentalButton);
        controlPanel.add(updateButton);
        controlPanel.add(deleteButton);
        controlPanel.add(filterButton);
        controlPanel.add(returnVehicleButton);
        controlPanel.add(returnButton);

        frame.add(controlPanel, BorderLayout.SOUTH);

        // 顯示界面
        frame.setSize(700, 400);
        frame.setVisible(true);
    }

    // 加載資料庫方法
    private void loadRentalsFromDatabase(){
        try {
            String query = "SELECT id, vehicle_id, customer_id, start_date, end_date, total_price FROM rentals";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getInt("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDouble("total_price")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "載入租賃資料失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }
    // 開始租賃
    private void startRental() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField vehicleIdField = new JTextField();
        JTextField customerIdField = new JTextField();
        JTextField startDateField = new JTextField();
        JTextField endDateField = new JTextField();
        JTextField rentalPriceField = new JTextField();

        panel.add(new JLabel("車輛ID:"));
        panel.add(vehicleIdField);
        panel.add(new JLabel("客戶ID:"));
        panel.add(customerIdField);
        panel.add(new JLabel("開始日期 (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("結束日期 (YYYY-MM-DD):"));
        panel.add(endDateField);
        panel.add(new JLabel("租金:"));
        panel.add(rentalPriceField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "開始租賃", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int vehicleId = Integer.parseInt(vehicleIdField.getText());
                int customerId = Integer.parseInt(customerIdField.getText());
                String startDate = startDateField.getText();
                String endDate = endDateField.getText();
                double rentalPrice = Double.parseDouble(rentalPriceField.getText());

                conn.setAutoCommit(false);  // 🔴 開啟交易模式

                // 插入租賃紀錄
                String insertRentalQuery = "INSERT INTO rentals (vehicle_id, customer_id, start_date, end_date, total_price) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement rentalStmt = conn.prepareStatement(insertRentalQuery);
                rentalStmt.setInt(1, vehicleId);
                rentalStmt.setInt(2, customerId);
                rentalStmt.setString(3, startDate);
                rentalStmt.setString(4, endDate);
                rentalStmt.setDouble(5, rentalPrice);
                rentalStmt.executeUpdate();

                // 更新車輛狀態
                String updateVehicleStatusQuery = "UPDATE vehicles SET status = '已租' WHERE id = ?";
                PreparedStatement vehicleStmt = conn.prepareStatement(updateVehicleStatusQuery);
                vehicleStmt.setInt(1, vehicleId);
                vehicleStmt.executeUpdate();

                conn.commit();  // 🔴 提交交易
                conn.setAutoCommit(true);

                JOptionPane.showMessageDialog(frame, "租賃成功！車輛已標記為「已租」", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadRentalsFromDatabase(); // 重新載入表格
            } catch (SQLException | NumberFormatException e) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                JOptionPane.showMessageDialog(frame, "租賃失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // 插入新租賃資料
    private void insertRentalToDatabase(int vehicleId, int customerId, String startDate, String endDate, double rentalPrice) {
        try {
            String query = "INSERT INTO rentals (vehicle_id, customer_id, start_date, end_date, total_price) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, startDate);
            pstmt.setString(4, endDate);
            pstmt.setDouble(5, rentalPrice);
            String updateVehicleStatusQuery = "UPDATE vehicles SET status = '已租' WHERE id = ?";
            PreparedStatement vehicleStmt = conn.prepareStatement(updateVehicleStatusQuery);
            vehicleStmt.setInt(1, vehicleId);
            pstmt.executeUpdate();
            System.out.println("成功新增租賃記錄！");
            JOptionPane.showMessageDialog(frame, "租賃成功！車輛已標記為「已租」", "成功", JOptionPane.INFORMATION_MESSAGE);
            loadRentalsFromDatabase(); // 重新載入表格
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "新增租賃失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 修改租賃資料
    private void updateRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "請選擇要修改的租賃記錄！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 取得選中行的數據
        int rentalId = (int) tableModel.getValueAt(selectedRow, 0);
        int vehicleId = (int) tableModel.getValueAt(selectedRow, 1);
        int customerId = (int) tableModel.getValueAt(selectedRow, 2);
        String startDate = tableModel.getValueAt(selectedRow, 3).toString();
        String endDate = tableModel.getValueAt(selectedRow, 4).toString();
        double rentalPrice = (double) tableModel.getValueAt(selectedRow, 5);

        // 創建輸入對話框
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField vehicleIdField = new JTextField(String.valueOf(vehicleId));
        JTextField customerIdField = new JTextField(String.valueOf(customerId));
        JTextField startDateField = new JTextField(startDate);
        JTextField endDateField = new JTextField(endDate);
        JTextField rentalPriceField = new JTextField(String.valueOf(rentalPrice));

        panel.add(new JLabel("車輛ID:"));
        panel.add(vehicleIdField);
        panel.add(new JLabel("客戶ID:"));
        panel.add(customerIdField);
        panel.add(new JLabel("開始日期 (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("結束日期 (YYYY-MM-DD):"));
        panel.add(endDateField);
        panel.add(new JLabel("租金:"));
        panel.add(rentalPriceField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "修改租賃記錄", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int newVehicleId = Integer.parseInt(vehicleIdField.getText());
                int newCustomerId = Integer.parseInt(customerIdField.getText());
                String newStartDate = startDateField.getText();
                String newEndDate = endDateField.getText();
                double newRentalPrice = Double.parseDouble(rentalPriceField.getText());

                conn.setAutoCommit(false);  // 開啟交易模式

                // 更新租賃資料
                String updateRentalQuery = "UPDATE rentals SET vehicle_id = ?, customer_id = ?, start_date = ?, end_date = ?, total_price = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(updateRentalQuery);
                pstmt.setInt(1, newVehicleId);
                pstmt.setInt(2, newCustomerId);
                pstmt.setString(3, newStartDate);
                pstmt.setString(4, newEndDate);
                pstmt.setDouble(5, newRentalPrice);
                pstmt.setInt(6, rentalId);
                pstmt.executeUpdate();

                // 若更換了車輛，則更新舊車與新車的狀態
                if (newVehicleId != vehicleId) {
                    // 設舊車為 "可租"
                    String resetOldVehicle = "UPDATE vehicles SET status = '可租' WHERE id = ?";
                    PreparedStatement resetOldStmt = conn.prepareStatement(resetOldVehicle);
                    resetOldStmt.setInt(1, vehicleId);
                    resetOldStmt.executeUpdate();

                    // 設新車為 "已租"
                    String updateNewVehicle = "UPDATE vehicles SET status = '已租' WHERE id = ?";
                    PreparedStatement updateNewStmt = conn.prepareStatement(updateNewVehicle);
                    updateNewStmt.setInt(1, newVehicleId);
                    updateNewStmt.executeUpdate();
                }

                conn.commit();  // 提交交易
                conn.setAutoCommit(true);

                JOptionPane.showMessageDialog(frame, "租賃修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException | NumberFormatException e) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                JOptionPane.showMessageDialog(frame, "修改租賃失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // 刪除租賃資料
    private void deleteRental() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "請選擇要刪除的租賃記錄！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rentalId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(frame, "確定刪除這筆租賃？", "確認刪除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String query = "DELETE FROM rentals WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, rentalId);
            pstmt.executeUpdate();

            loadRentalsFromDatabase(); // 重新載入
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "刪除租賃失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnVehicle() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "請選擇要歸還的租賃記錄！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int rentalId = (int) tableModel.getValueAt(selectedRow, 0);
        int vehicleId = (int) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(frame, "確定歸還這台車？", "確認歸還", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            conn.setAutoCommit(false);  // 🔴 開啟交易模式

            // 更新租賃狀態（可根據需求新增租賃記錄的 `status` 欄位）
            String updateRentalQuery = "UPDATE rentals SET end_date = CURDATE() WHERE id = ?";
            PreparedStatement rentalStmt = conn.prepareStatement(updateRentalQuery);
            rentalStmt.setInt(1, rentalId);
            rentalStmt.executeUpdate();

            // 更新車輛狀態為 "可租"
            String updateVehicleQuery = "UPDATE vehicles SET status = '可租' WHERE id = ?";
            PreparedStatement vehicleStmt = conn.prepareStatement(updateVehicleQuery);
            vehicleStmt.setInt(1, vehicleId);
            vehicleStmt.executeUpdate();

            conn.commit();  // 🔴 提交交易
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(frame, "車輛歸還成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            loadRentalsFromDatabase();  // 重新載入表格
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(frame, "歸還失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // 篩選租賃
    private void filterRentals() {
        String statusFilter = JOptionPane.showInputDialog(frame, "請輸入篩選條件（進行中/已結束）：");
        if (statusFilter == null || statusFilter.isEmpty()) return;

        try {
            String query = "SELECT id, vehicle_id, customer_id, start_date, end_date, total_price FROM rentals " +
                    (statusFilter.equals("進行中") ? "WHERE end_date >= CURDATE()" : "WHERE end_date < CURDATE()");

            tableModel.setRowCount(0); // 清空表格
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getInt("customer_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDouble("total_price")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "篩選失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new RentalManagementSystem();
    }
}
