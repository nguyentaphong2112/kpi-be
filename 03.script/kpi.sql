ALTER TABLE hbt_kpi.kpi_employee_evaluations MODIFY COLUMN status varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'trạng thái';
ALTER TABLE hbt_kpi.kpi_approval_histories MODIFY COLUMN status varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Trạng thái phê duyệt';

ALTER TABLE hbt_kpi.kpi_employee_indicators MODIFY COLUMN target longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Mục tiêu';

ALTER TABLE hbt_kpi.kpi_work_planning_templates ADD code varchar(50) NULL COMMENT 'Mã biểu mẫu';
ALTER TABLE hbt_kpi.kpi_employee_work_plannings ADD percent decimal(5,2) DEFAULT NULL NULL COMMENT 'phần trăm';
ALTER TABLE hbt_kpi.kpi_employee_work_plannings ADD is_selected enum('N','Y') CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT 'N' NOT NULL COMMENT 'Lựa chọn (Y : Chọn, N : Chưa chọn)';

ALTER TABLE hbt_kpi.kpi_employee_evaluations ADD reason varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Lý do điều chỉnh';
ALTER TABLE hbt_kpi.kpi_organization_evaluations ADD reason varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'Lý do điều chỉnh';

Create or replace view sys_category_attributes as select  * from hbt_admin.sys_category_attributes

ALTER TABLE hbt_kpi.kpi_organization_indicators ADD status_level1 varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'trạng thái cấp 1';

