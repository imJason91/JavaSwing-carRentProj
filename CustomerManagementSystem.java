import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerManagementSystem {
    JFrame frame;
    JTable customerTable;
    DefaultTableModel tableModel;

    // MySQL 連線資訊
    Connection conn;

    public CustomerManagementSystem() {
        frame = new JFrame("客戶管理");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 設置表格模型
        String[] columnNames = {"編號", "姓名", "電話", "租賃狀況", "操作"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // 初始化表格
        customerTable = new JTable(tableModel);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = customerTable.getSelectedRow();
                if (row != -1) {
                    String customerId = customerTable.getValueAt(row, 0).toString();
                    int option = JOptionPane.showOptionDialog(frame,
                            "選擇操作：", "操作選單",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null, new String[]{"修改", "刪除", "取消"}, "修改");

                    if (option == 0) {
                        editCustomer(customerId);
                    } else if (option == 1) {
                        deleteCustomer(customerId);
                    }
                }
            }
        });

        // 加載資料庫客戶資料
        loadCustomersFromDB();

        // 顯示控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // 新增客戶按鈕
        JButton addCustomerButton = new JButton("新增客戶");
        addCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });

        // 篩選按鈕
        JButton filterButton = new JButton("篩選");
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterCustomers();
            }
        });

        // 返回主頁按鈕
        JButton returnButton = new JButton("返回主頁");
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        controlPanel.add(addCustomerButton);
        controlPanel.add(filterButton);
        controlPanel.add(returnButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // 顯示界面
        frame.setSize(600, 400);
        frame.setVisible(true);
    }

    // 載入客戶資料
    private void loadCustomersFromDB(){
        conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        try(Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM customers")){
            tableModel.setRowCount(0);
            while(rs.next()){
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("rental_history"),
                        "點擊刪除/修改"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void addCustomer(){
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField rentalStatusField = new JTextField();

        panel.add(new JLabel("姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("電話:"));
        panel.add(phoneField);
        panel.add(new JLabel("租賃狀況:"));
        panel.add(rentalStatusField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "新增客戶", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION){
            addCustomerToDB(nameField.getText(), phoneField.getText(), rentalStatusField.getText());
            loadCustomersFromDB(); // 重載
        }
    }

    private boolean isPhoneExists(String phone) {
        conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM customers WHERE phone = ?")) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addCustomerToDB(String name, String phone, String rentalHistory){
        conn = DatabaseConnection.getConnection();
        if (conn == null) return;

        if (isPhoneExists(phone)) {
            JOptionPane.showMessageDialog(frame, "電話號碼已存在！請輸入不同的號碼。", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO customers (name, phone, rental_history) VALUES (?, ?, ?)")) {
                pstmt.setString(1, name);
                pstmt.setString(2, phone);
                pstmt.setString(3, rentalHistory);
                pstmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改客戶資訊
    private void editCustomer(String customerId) {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField rentalStatusField = new JTextField();

        panel.add(new JLabel("新姓名:"));
        panel.add(nameField);
        panel.add(new JLabel("新電話:"));
        panel.add(phoneField);
        panel.add(new JLabel("新租賃狀況:"));
        panel.add(rentalStatusField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "修改客戶", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE customers SET name=?, phone=?, rental_history=? WHERE id=?")) {
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, phoneField.getText());
                pstmt.setString(3, rentalStatusField.getText());
                pstmt.setInt(4, Integer.parseInt(customerId));
                pstmt.executeUpdate();
                loadCustomersFromDB();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 刪除客戶
    private void deleteCustomer(String customerId) {
        int confirm = JOptionPane.showConfirmDialog(frame, "確定刪除此客戶？", "確認刪除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customers WHERE id=?")) {
                pstmt.setInt(1, Integer.parseInt(customerId));
                pstmt.executeUpdate();
                loadCustomersFromDB();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 篩選客戶
    private void filterCustomers() {
        String nameFilter = JOptionPane.showInputDialog(frame, "輸入篩選姓名:");
        if (nameFilter != null && !nameFilter.isEmpty()) {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM customers WHERE name LIKE ?")) {
                pstmt.setString(1, "%" + nameFilter + "%");
                ResultSet rs = pstmt.executeQuery();

                tableModel.setRowCount(0);
                while (rs.next()) {
                    tableModel.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("rental_history"), "點擊修改/刪除"});
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new CustomerManagementSystem();
    }
}
