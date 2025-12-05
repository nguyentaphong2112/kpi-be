insert into hbt_admin.sys_categories(
    category_id, category_type, code, name, value, order_number, is_deleted, created_by, created_time
)
select
    null, category_type, code, name, value, order_number, 'N', 'tudd', now()
from
    vcc_utils.sys_categories
where
    category_type in (
                      'LOAI_DS_TRICH_NOP', 'TRANG_THAI_THU_BHXH', 'NOI_TGIA_BHXH', 'ICN_PHAN_LOAI_LD',
                      'DOI_TUONG', 'THUE_DOI_TUONG', 'THUE_DON_VI_KE_KHAI', 'THUE_TRANG_THAI',
                      'THUE_CACH_TINH', 'TRANG_THAI_KKT', 'THUE_KIEU_NHAP_LIEU',
                      'MOI_QUAN_HE', 'LOAI_THAY_DOI',
                      'TRANG_THAI_TRICH_NOP', 'PHAN_NHOM_CHUC_DANH', 'TRANG_THAI_RA_SOAT_DU_LIEU',
                      'THUE_LOAI_THU_NHAP'
        )
;

INSERT into hbt_admin.sys_config_parameters (
    config_parameter_id, config_group, config_group_name, config_period_type, config_columns, is_deleted, created_by, created_time, module_code
)
select null, config_group, config_group_name, config_period_type, config_columns, 'N', 'tudd', now(), module_code
from hcm_icn.icn_config_parameters
where is_deleted = 'N';

insert into hbt_admin.sys_parameters(
    start_date, config_group, config_code, config_name, data_type, config_value, is_deleted, created_by, created_time
)
select start_date, config_group, config_code, config_name, data_type, config_value, 'N', 'tudd', now()
from hcm_icn.icn_parameters;