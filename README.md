##I. Quy tắc đặt tên API

    1. Các API cung cấp theo chuẩn REST FULL API và tuân thủ theo các nguyên tắc:
		- GET: Sử dụng cho các API lấy dữ liệu, tìm kiếm resource
		- POST: Sử dụng cho các API tạo mới resource
		- PUT: Cập nhật resource
		- DELETE: Xóa resource
		Những phương thức hay hoạt động này thường được gọi là CRUD tương ứng với Create, Read, Update, Delete – Tạo, Đọc, Sửa, Xóa.
		
		Lưu ý rằng, GET và DELETE sẽ không có payload body. các Input được đặt trong parameter.
	
	2. Các API URL cần đặt theo format: /{version}/{nghiệp vụ}/{resource}
	
		Ví dụ: 
			- URI: /v1/project-members/employees/{employeeId}, METHOD: GET => lấy thông tin quá trình tham gia dự án của một nhân viên cụ thể nào đó
			- URI: /v1/project-members, METHOD: GET => Tìm kiếm thông tin quá trình tham gia dự án
			- URI: /v1/project-members, METHOD: POST => Lưu thông tin quá trình tham gia dự án
			- URI: /v1/project-members/{id}, METHOD: DELETE => Xóa bản ghi theo id
			- URI: /v1/project-members/{id}, METHOD: GET => Lấy chi tiết bản ghi theo id
##II. Quy tác quản lý git

	1.	Mỗi project sẽ bao gồm các nhánh như sau:
		1.1. master: nhánh chính chứa source code ổn định, đã được kiểm tra và bàn giao sang MB
		1.2. develop: nhánh chính chứa source code mới nhất của nhiều đội dự án khác nhau do tudd quản lý. Nhánh này không được phép commit trực tiếp lên. Chỉ được phép tạo merge request từ {Mã đối tác}-develop.
		1.3. {Mã đối tác}-develop: 
			- Nhánh chính do Đối tác OS quản lý, có nhiệm vụ merge giữa các nhánh của thành viên Đối tác, tạo merge request sang develop để bàn giao nghiệm thu, Build ứng dụng lên server test. 
			- Source code của nhánh này được hiểu là code đã được leader review.
			- Trước khi tạo merge request sang develop cần phải pass qua rule của sonar queue.
			- Không được phép commit code trực tiếp trên nhánh này
			- Khuyến nghị nên pull source từ develop về trước khi tạo merge request hoặc build lên server test.
		1.4. Các nhánh khác: 
			- Mỗi thành thành viên của đối tác có thể tự tạo nhánh cho riêng mình trên cơ sở base từ {Mã đối tác}-develop. 
			- Việc đặt tên nhánh do {Mã đối tác} tự quyết. Nhưng nếu đặt tên theo kiểu Task/Task-Name thì sau khi hoàn thành task phải thực hiện xóa nhánh đi. 
			  Còn nếu đặt tên theo kiểu mỗi thành viên là 1 branch thì có thể duy trì nhánh xuyên suốt dự án. 
			- Hằng ngày trước khi làm việc thì khuyến nghị nên pull source từ {Mã đối tác}-develop về.
##III. Thay đổi database

    1. Các bảng tạo mới hoặc bổ sung thêm cột thì cần phải viết câu lệnh đặt ở trong thư mục database
    2. Rule đặt tên file: {tên sprint}_{ngày tạo)_nội dung thay đổi.sql


# Hướng dẫn cấu hình import dữ liệu

## 📌 Mục đích
Cho phép import dữ liệu từ file Excel theo cấu hình XML.

---

## 📁 Cấu hình file XML

```xml
<root>
    <tableName>is_eib_life_insurances</tableName>
    <firstDataRow>4</firstDataRow>
    <maxNumberOfRecord>10000</maxNumberOfRecord>
    <col title="STT" type="long"  length="10" />
    <col title="Mã nhân viên" type="string" length="20" nullable="false"/>
    <col title="Tên" type="string" length="200" nullable="true"/>
    <col title="Sản phẩm tham gia" type="string" length="200" nullable="false"/>
    <col title="Mức hưởng" type="long" length="200" nullable="false" min = "1" max = "9999999999"/>
    <col title="Số HD bảo hiểm" type="string" length="20" nullable="false" duplicate="false"/>
    <col title="Kỳ xử lý" type="mmyyyy" length="7" nullable="false"/>
    <col title="Ngày tạo" type="ddMMyyyy hhmmss" length="20" nullable="false"/>
    <col title="Hiệu lực từ ngày" type="date" length="10" nullable="false"/>
    <col title="Hiệu lực đến ngày" type="date" length="10" nullable="false"/>
    <col title="Trạng thái tham gia" type="string"  length="200" nullable="false" textValueList="Đang tham gia, Đã đóng"/>
    <col title="Lý do dừng" type="string"  length="500" nullable="true"/>
</root>
```

---

## 📋 Giải thích cấu hình

| Thuộc tính XML         | Mô tả |
|------------------------|------|
| `tableName`            | Tên bảng trong CSDL: `is_eib_life_insurances` |
| `firstDataRow`         | Dòng bắt đầu đọc dữ liệu trong Excel (bỏ qua tiêu đề) |
| `maxNumberOfRecord`    | Số dòng dữ liệu tối đa được phép import |
| `col`                  | Cấu hình cho từng cột: tiêu đề, kiểu dữ liệu, độ dài, bắt buộc, trùng lặp |

---

## 🔍 Mô tả chi tiết các thuộc tính trong thẻ `<col>`

| Thuộc tính          | Bắt buộc              | Mô tả                                                                                                                          |
|---------------------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------|
| **`title`**         | ✔️                    | Tiêu đề của cột, phải **khớp chính xác** với tên cột trong file Excel. Dùng để ánh xạ dữ liệu vào đúng cột trong cơ sở dữ liệu. |
| **`type`**          | ✔️                    | Kiểu dữ liệu của cột. Hỗ trợ các kiểu phổ biến: `string`, `long`, `date`, `double`, `boolean`, `mmyyyy`, `ddMMyyyy hhmmss`.                               |
| **`length`**        | ✔️                    | Độ dài tối đa của dữ liệu (áp dụng với kiểu `string` hoặc `long`). Với kiểu `date` thì chỉ dùng để kiểm tra định dạng.         |
| **`nullable`**      | ✔️                    | Xác định cột có thể để trống không. Nếu `nullable="false"` thì bắt buộc phải có giá trị khi import.                            |
| **`duplicate`**     | ❌ (mặc định là `true`)| Cho biết dữ liệu cột này có được phép **trùng lặp** không. Nếu `duplicate="false"` thì mỗi giá trị phải là duy nhất.           |
| **`min`**           | ❌                     | Đối với kiểu `long` hoặc `double`, xác định giá trị tối thiểu của cột.            |
| **`max`**           | ❌                     | Đối với kiểu `long` hoặc `double`, xác định giá trị tối đa của cột.           |
| **`textValueList`** | ❌                     | Dùng cho kiểu string, cung cấp danh sách các giá trị hợp lệ để chọn (ví dụ: "Đang tham gia, Đã đóng").           |

### 💡 Ví dụ cấu hình cột đầy đủ:

```xml
<col title="Mã nhân viên" type="string" length="20" nullable="false" duplicate="true"/>
```

- Cột có tên **"Mã nhân viên"** trong Excel
- Kiểu **string**, tối đa **20 ký tự**
- **Bắt buộc** nhập (nullable = false)
- **Cho phép trùng lặp**

---

## 📌 Yêu cầu định dạng file Excel

- File Excel phải bắt đầu dữ liệu từ dòng số **4**.
- Các cột phải khớp tiêu đề **chính xác** như khai báo trong XML:
    - STT
    - Mã nhân viên
    - Tên
    - Sản phẩm tham gia
    - Mức hưởng
    - Số HD bảo hiểm
    - Hiệu lực từ ngày
    - Hiệu lực đến ngày
    - Trạng thái tham gia
    - Lý do dừng

---

## ✅ Ràng buộc dữ liệu

- **Mã nhân viên, Sản phẩm tham gia, Mức hưởng, Số HD bảo hiểm, Hiệu lực từ ngày, Hiệu lực đến ngày, Trạng thái tham gia**: bắt buộc nhập.
- **Số HD bảo hiểm**: không được trùng lặp.
- **Ngày** phải theo định dạng `dd/MM/yyyy` (ví dụ: `12/02/2025`).
- **Ngày tạo** phải theo định dạng `dd/MM/yyyy HH:mi:ss` (ví dụ: `12/02/2025 14:40:20`).
- **Kỳ xử lý** phải theo định dạng `MM/yyyy HH:mi:ss` (ví dụ: `02/2025`).
- **Trạng thái tham gia** chỉ được phép là "Đang tham gia" hoặc "Đã đóng".


