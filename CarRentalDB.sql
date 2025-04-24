-- 建立資料庫
CREATE DATABASE CarRentalDB;
USE CarRentalDB;

-- 建立車輛表
CREATE TABLE vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    rental_price DECIMAL(10,2) NOT NULL,
    status ENUM('可租', '已租') NOT NULL DEFAULT '可租'
);

-- 建立客戶表
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    rental_history TEXT  -- 存放租賃紀錄的 JSON 或其他格式
);

-- 建立租賃表
CREATE TABLE rentals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    customer_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);