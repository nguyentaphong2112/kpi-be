CREATE OR REPLACE VIEW `hbt_admin`.`hr_jobs` AS
select * from `hbt_hrm`.`hr_jobs`;

CREATE OR REPLACE VIEW `hbt_admin`.`hr_organizations` AS
select * from `hbt_hrm`.`hr_organizations`;

CREATE OR REPLACE VIEW `hbt_admin`.`hr_employees` AS
select * from `hbt_hrm`.`hr_employees`;

ALTER TABLE hbt_admin.sys_dynamic_reports MODIFY COLUMN report_type enum('DOC','EXCEL','PDF') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL;


ALTER TABLE hbt_admin.sys_feedbacks ADD `type` varchar(255) NULL COMMENT 'Loại phản ánh';

ALTER TABLE hbt_admin.sys_object_attributes MODIFY COLUMN attribute_value longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL NULL COMMENT 'giá trị';


