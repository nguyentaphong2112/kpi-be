# 🛠️ HƯỚNG DẪN CÀI ĐẶT HỆ THỐNG HRM

---

## I. CÀI ĐẶT JAVA 17

Truy cập trang sau để tải JDK 17:

🔗 https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

> ✅ Có thể chọn bản portable nếu không muốn cài đặt hệ thống hoặc bị xung đột với phần mềm khác.

---

## II. CÀI ĐẶT MARIADB VÀ IMPORT DATABASE

### 1. Cài đặt MariaDB 10.x

Tải và cài đặt theo hướng dẫn tại:  
🔗 https://mariadb.com/kb/en/getting-installing-and-upgrading-mariadb/

---

### 2. Tạo user và cơ sở dữ liệu (PowerShell)

```sql
-- Đăng nhập MySQL
mysql -u root -p;

-- Tạo user
CREATE USER 'hrm'@'localhost' IDENTIFIED BY 'hrm#2025';

-- Tạo các database
CREATE DATABASE db_hrm CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE db_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE db_abs CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE db_med CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE db_lms CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE db_kpi CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Gán quyền cho user hrm
GRANT ALL PRIVILEGES ON db_hrm.* TO 'hrm'@'localhost';
GRANT ALL PRIVILEGES ON db_admin.* TO 'hrm'@'localhost';
GRANT ALL PRIVILEGES ON db_abs.* TO 'hrm'@'localhost';
GRANT ALL PRIVILEGES ON db_med.* TO 'hrm'@'localhost';
GRANT ALL PRIVILEGES ON db_lms.* TO 'hrm'@'localhost';
GRANT ALL PRIVILEGES ON db_kpi.* TO 'hrm'@'localhost';

-- Đảm bảo charset
ALTER DATABASE db_hrm CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER DATABASE db_admin CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER DATABASE db_abs CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER DATABASE db_lms CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER DATABASE db_med CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;


cmd /c "mysql -u hrm -p db_admin < hbt_admin.sql"
cmd /c "mysql -u hrm -p --default-character-set=utf8mb4 db_hrm < hbt_hrm.sql"
cmd /c "mysql -u hrm -p --default-character-set=utf8mb4 db_kpi < hbt_kpi.sql"
cmd /c "mysql -u hrm -p --default-character-set=utf8mb4 db_med < hbt_med.sql"
cmd /c "mysql -u hrm -p --default-character-set=utf8mb4 db_lms < hbt_lms.sql"
cmd /c "mysql -u hrm -p --default-character-set=utf8mb4 db_abs < hbt_abs.sql"
```


## III. Cài đặt nginx và FE

Sửa lại file nginx.config (Sửa phần location)

File mẫu:
[nginx.conf.txt](./sampleDocument/nginx.conf.txt)

Vào source code bên mình chạy lệnh npm run build

Lấy file trong dist và chuyển vào file theo nginx cấu hình

Reload nginx : chạy cmd : nginx -s reload

chạy lệnh reset all mật khẩu:

```
curl -X PUT http://localhost:8966/api/admin-service/v1/user/reset-password/all?password=123456 -H "Content-Type: application/json" -H "Authorization: Bearer eyJ0eXBlIjoiSldTIiwiYWxnIjoiSFM1MTIifQ.eyJqdGkiOiJocm0iLCJleHAiOjE3NDQ5NDU2OTksIm5iZiI6MTc0NDkzOTY5OSwiaWF0IjoxNzQ0OTM5Njk5LCJ1c2VySW5mbyI6IntcImxvZ2luTmFtZVwiOlwiaHJtXCIsXCJ1c2VySWRcIjoxLFwiZnVsbE5hbWVcIjpcIlF14bqjbiB0cuG7iyBo4buHIHRo4buRbmdcIixcInN0YXR1c1wiOlwiQUNUSVZFXCIsXCJyb2xlQ29kZUxpc3RcIjpbXCJhZG1pblwiXSxcImlkXCI6XCJocm1cIn0iLCJjbGFzc05hbWUiOiJ2bi5oYnRwbHVzLm1vZGVscy5kdG8uVXNlckR0byJ9.TTfC5LTJgWtUjTZf2e8rU6e7r64nfFDkBeivBvMD2Ce2ifHEIto8nEis7HgO_YbNhJu3k14JCo2469GV7740-w" -d '{}'
curl -X PUT "http://localhost:8966/api/admin-service/v1/user/reset-password/all?password=123456"
-H "Content-Type: application/json" \
-d '{}'
```


## IV. Cài đặt BE

Vào source code bên mình chạy lệnh nvm clean install

Sau đó lấy file jar cho vào file HRM/backend/jar

Tạo thêm folder scripts và file pid (Lưu ý phải config lại URL, USER, PASSWORD các file trong scripts)

Sau khi config thì chạy git bash và chạy lệnh ví dụ như: ./admin-service.sh stop, ./admin-service.sh start

File mẫu:
[admin-service.sh.txt](./sampleDocument/admin-service.sh.txt)


