import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CarManagementSystem {
    JFrame frame;
    JTable carTable;
    DefaultTableModel tableModel;

    public CarManagementSystem() {
        frame = new JFrame("車輛管理");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 設定表格
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"ID", "品牌", "型號", "租金", "狀態"});
        carTable = new JTable(tableModel);

        // 初始化表格
        carTable = new JTable(tableModel);
        carTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(carTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        loadVehicles(); // 載入車輛資料

        // 顯示控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // 新增車輛的按鈕
        JButton addCarButton = new JButton("新增車輛");
        addCarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCar();
            }
        });

        // 篩選按鈕
        JButton filterButton = new JButton("篩選");
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterCars();
            }
        });

        // 返回主頁的按鈕
        JButton returnButton = new JButton("返回主頁");
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        controlPanel.add(addCarButton);
        controlPanel.add(filterButton);
        controlPanel.add(returnButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // 顯示界面
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    // 新增車輛函數
    private void addCar() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField rentField = new JTextField();
        String[] statuses = {"可租", "已租"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);

        panel.add(new JLabel("品牌:"));
        panel.add(brandField);
        panel.add(new JLabel("車輛型號:"));
        panel.add(modelField);
        panel.add(new JLabel("租金:"));
        panel.add(rentField);
        panel.add(new JLabel("狀態:"));
        panel.add(statusCombo);

        int option = JOptionPane.showConfirmDialog(frame, panel, "新增車輛", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String brand = brandField.getText();
            String model = modelField.getText();
            double rent;
            try {
                rent = Double.parseDouble(rentField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "租金必須是數字！", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String status = (String) statusCombo.getSelectedItem();

            // 插入資料庫
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) return;

            String insertQuery = "INSERT INTO vehicles (brand, model, rental_price, status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, brand);
                pstmt.setString(2, model);
                pstmt.setDouble(3, rent);
                pstmt.setString(4, status);
                int rowsInserted = pstmt.executeUpdate();

                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(frame, "車輛新增成功！");
                    // 重新載入表格
                    tableModel.setRowCount(0);
                    loadVehicles();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void filterCars() {
        String status = JOptionPane.showInputDialog(frame, "輸入篩選狀態（可租/已租）：");
        if (status == null || status.isEmpty()) return;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String query = "SELECT * FROM vehicles WHERE status = ?";
        tableModel.setRowCount(0); // 清空表格，重新載入篩選結果

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("rental_price"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVehicles() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        String query = "SELECT * FROM vehicles";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("rental_price"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CarManagementSystem();
    }
}
