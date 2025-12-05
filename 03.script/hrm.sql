ALTER TABLE hbt_hrm.hr_employees ADD job_title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Chức danh (trong trường hợp nhập text)';
ALTER TABLE hbt_hrm.hr_employees ADD department_name varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Đơn vị công tác (trong trường hợp nhập text)';
ALTER TABLE hbt_hrm.hr_insurance_salary_process ADD insurance_factor decimal(4,2) NULL;
ALTER TABLE hbt_hrm.hr_insurance_salary_process ADD insurance_base_salary int(20) NULL;
ALTER TABLE hbt_hrm.hr_allowance_process ADD factor decimal(4,2) DEFAULT NULL NULL;
Create or replace view sys_object_attributes as select  * from hbt_admin.sys_object_attributes


CREATE TABLE `log_tasks` (
        `log_task_id` int(11) NOT NULL AUTO_INCREMENT,
        `name` varchar(200) DEFAULT NULL,
        `project_code` varchar(200) DEFAULT NULL,
        `log_date` date DEFAULT NULL,
        `description` varchar(500) DEFAULT NULL,
        `is_deleted` enum('N','Y') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y : đã xóa, N : chưa xóa)',
        `created_by` varchar(50) DEFAULT NULL,
        `created_time` timestamp NULL DEFAULT NULL COMMENT 'ngày tạo',
        `modified_by` varchar(50) DEFAULT NULL,
        `modified_time` timestamp NULL DEFAULT NULL COMMENT 'ngày sửa',
        `total_house` int(2) DEFAULT NULL,
        PRIMARY KEY (`log_task_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2899 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_vietnamese_ci ROW_FORMAT=DYNAMIC;

INSERT INTO hbt_hrm.sys_categories
(category_id, category_type, code, name, value, order_number, note, is_deleted, created_by, created_time, modified_by, modified_time, last_update_time, parent_id, path_id, path_level, path_order)
VALUES(44251, 'HRM_LOG_TASK', 'SKYT', 'Sáng kiến ý tưởng', 'SKYT', 1, NULL, 'N', NULL, NULL, NULL, NULL, '2025-10-06 15:06:40.000', NULL, NULL, NULL, NULL);
