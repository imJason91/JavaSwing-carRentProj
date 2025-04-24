import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class MainPage {

    public static void main(String[] args){
        JFrame frame = new JFrame("\uD83C\uDFCE\uFE0F 汽車租賃管理系統");
        frame.setSize(1400,880);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BackgroundPanel backgroundPanel = new BackgroundPanel("altis.jpg");
        backgroundPanel.setLayout(new BorderLayout());

        //標題的設置
        JLabel titleLabel = new JLabel("\uD83C\uDFCE\uFE0F 汽車租賃管理系統", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(Color.RED); // 紅色標題（賽車感）
        frame.add(titleLabel, BorderLayout.NORTH);

        //設置按鈕區域
        JPanel navigationPanel = new JPanel();
        navigationPanel.setOpaque(false);
        navigationPanel.setLayout(new FlowLayout());

        JButton vehicleButton = createStyledButton("🚗 車輛管理");
        JButton customerButton = createStyledButton("👤 客戶管理");
        JButton rentalButton = createStyledButton("📅 租賃管理");
        JButton settingsButton = createStyledButton("⚙️ 設定");

        navigationPanel.add(vehicleButton);
        navigationPanel.add(customerButton);
        navigationPanel.add(rentalButton);
        navigationPanel.add(settingsButton);
        backgroundPanel.add(navigationPanel, BorderLayout.CENTER);

        // 設置統計數據區域
        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new GridLayout(4, 1));
        int[] stats = getStatsFromDB();

        JLabel availableVehiclesLabel = createStyledLabel("當前可租車輛數: " + stats[0]);
        JLabel rentedVehiclesLabel = createStyledLabel("當前租賃車輛數: " + stats[1]);
        JLabel customerCountLabel = createStyledLabel("當前客戶數量: " + stats[2]);
        JLabel rentalTodayLabel = createStyledLabel("今日租賃數量: " + stats[3]);

        statsPanel.add(availableVehiclesLabel);
        statsPanel.add(rentedVehiclesLabel);
        statsPanel.add(customerCountLabel);
        statsPanel.add(rentalTodayLabel);

        backgroundPanel.add(statsPanel, BorderLayout.SOUTH);
        frame.setContentPane(backgroundPanel);
        frame.setVisible(true);

        // 按鈕事件監聽
        vehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CarManagementSystem CarMS = new CarManagementSystem();
                // 顯示車輛管理頁面
                CarMS.frame.setVisible(true);
            }
        });

        customerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomerManagementSystem CMS = new CustomerManagementSystem();
                CMS.frame.setVisible(true);
            }
        });

        rentalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RentalManagementSystem RMS = new RentalManagementSystem();
                RMS.frame.setVisible(true);
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, "進入設定頁面");
            }
        });

        Timer timer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] newStats = getStatsFromDB();
                availableVehiclesLabel.setText("當前可租車輛數: " + newStats[0]);
                rentedVehiclesLabel.setText("當前租賃車輛數: " + newStats[1]);
                customerCountLabel.setText("當前客戶數量: " + newStats[2]);
                rentalTodayLabel.setText("今日租賃數量: " + newStats[3]);
            }
        });
        timer.start();
    }

    static class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String fileName) {
            backgroundImage = new ImageIcon(fileName).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.RED);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setPreferredSize(new Dimension(180, 50));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(200, 0, 0)); // 變深紅
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.RED);
            }
        });

        return button;
    }

    private static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(50, 50, 50)); // 深灰色背景
        return label;
    }

    public static int[] getStatsFromDB(){
        int availableVehicles = 0;
        int rentedVehicles = 0;
        int customerCount = 0;
        int rentalToday = 0;

        String url = "jdbc:mysql://localhost:3306/CarRentalDB";
        String user = "root";
        String password = "196203";

        try{
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles WHERE status='可租'");
            if(rs.next()){
                availableVehicles = rs.getInt(1);
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles WHERE status='已租'");
            if(rs.next()){
                rentedVehicles = rs.getInt(1);
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
            if(rs.next()){
                customerCount = rs.getInt(1);
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rentals WHERE start_date >= CURDATE()");
            if(rs.next()){
                rentalToday = rs.getInt(1);
            }
            rs.close();

            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }

        return new int[]{availableVehicles, rentedVehicles, customerCount, rentalToday};
    }

}
