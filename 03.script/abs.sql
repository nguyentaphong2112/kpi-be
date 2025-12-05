ALTER TABLE hbt_abs.abs_work_calendars CHANGE default_hodiday_date default_holiday_date varchar(2000) CHARACTER SET utf8mb3 COLLATE utf8mb3_vietnamese_ci DEFAULT NULL NULL COMMENT 'các ngày nghỉ lễ mặc định trong năm';
CREATE FULLTEXT INDEX abs_reason_types_code_IDX ON hbt_abs.abs_reason_types (code,name);
ALTER TABLE hbt_abs.abs_workday_types MODIFY COLUMN workday_type_id int(11) auto_increment NOT NULL COMMENT 'id khóa chính';
ALTER TABLE hbt_abs.abs_reason_types MODIFY COLUMN reason_type_id int(11) auto_increment NOT NULL COMMENT 'id khóa chính';
ALTER TABLE hbt_abs.abs_timekeepings MODIFY COLUMN total_hours double(7,2) DEFAULT NULL NULL COMMENT 'số giờ chấm công';
ALTER TABLE hbt_abs.abs_request_handovers ADD is_deleted enum('N','Y') DEFAULT 'N' NOT NULL;
ALTER TABLE hbt_abs.abs_request_handovers MODIFY COLUMN request_handover_id int(11) auto_increment NOT NULL COMMENT 'id khóa chính';
ALTER TABLE hbt_abs.abs_request_approvers ADD is_deleted enum('N','Y') NOT NULL COMMENT 'Trạng thái xóa (Y : đã xóa, N : chưa xóa)';
ALTER TABLE hbt_abs.abs_request_approvers MODIFY COLUMN request_approver_id int(11) auto_increment NOT NULL COMMENT 'id khóa chính';

ALTER TABLE hbt_abs.abs_workday_types ADD `type` varchar(50) NULL COMMENT 'Loại';







