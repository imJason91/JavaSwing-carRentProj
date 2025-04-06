# JavaSwing-carRentProj
利用JAVA開發簡易的資料庫應用系統(主題:車輛租賃管理系統) 運用技術:Mysql Java swing(gui) 

本專案為一個使用 Java Swing 框架開發的桌面應用程式，模擬基本的汽車租賃管理功能，包含車輛資料管理、顧客租車登記、租賃記錄查詢等。使用 MySQL 作為後端資料庫，並結合 JDBC 實現資料存取。

## 功能簡介

- 車輛資訊新增、查詢、修改、刪除（CRUD）
- 顧客租車資料登記與管理
- 租賃記錄查詢
- Swing 視覺介面呈現

## 技術棧

- Java 17+
- Swing
- MySQL
- JDBC

## 使用方式

1. 匯入專案至 IntelliJ IDEA 或其他支援的 IDE。
2. 確保本地已啟動 MySQL，並匯入 `sql` 資料夾中的資料表。
3. 修改 `DBConnect.java` 中的資料庫連線資訊。
4. 執行 `Main.java` 開始使用程式。

## 注意事項

- 請先建立對應的 MySQL 資料庫與資料表。
- 預設帳號密碼可在資料庫中查閱或於登入功能中自訂。

---
