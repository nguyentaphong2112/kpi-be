package vn.hbtplus.tax.income.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.services.AuthorizationService;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.tax.income.models.request.RIncomeTaxRequest;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RIncomeTaxRepository extends BaseRepository {
    final String SO_LIEU_TONG_HOP = """
            sum(T.tong_luong) as tong_luong,
            sum(T.thu_nhap_khac) as thu_nhap_khac,
            sum(T.luong_chiu_thue) as luong_chiu_thue,
            sum(T.luong_mien_thue) as luong_mien_thue,
            sum(T.phu_cap_an_ca) as phu_cap_an_ca,
            sum(T.phu_cap_dien_thoai) as phu_cap_dien_thoai,
            sum(T.phu_cap_khac) as phu_cap_khac,
            sum(T.bao_hiem) as bao_hiem,
            sum(T.thue_ke_khai) as thue_ke_khai,
            sum(T.thue_truy_thu_thang) as thue_truy_thu_thang,
            sum(T.thue_truy_thu_nam) as thue_truy_thu_nam,
            null as ghi_chu
            """;
    private final AuthorizationService authorizationService;

    public RIncomeTaxRepository(AuthorizationService authorizationService) {
        super();
        this.authorizationService = authorizationService;
    }

    public List<Map<String, Object>> getListPersonalIncome(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.ma_nv, T.ho_va_ten,
                    T.so_cmt, T.ma_so_thue,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.ma_nv, T.ho_va_ten, T.so_cmt, T.ma_so_thue
                order by ifnull(T.ma_nv,'N/A'), T.ho_va_ten
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    private String getSQLQuery(RIncomeTaxRequest.ReportByIdForm reportForm, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("""
                select
                	itd.emp_code ma_nv,
                	itd.full_name ho_va_ten,
                	IFNULL(pi.identity_no, itd.personal_id_no) so_cmt,
                    IFNULL(e.tax_no, itd.tax_no) ma_so_thue,
                	itd.income_item_master_id,
                	itd.accounting_income_name khoan_tai_chinh,
                	DATE_FORMAT(itd.tax_period_date,'%m/%Y') ky_tra,
                	DATE_FORMAT(ii.salary_period_date,'%m/%Y') ky_luong,
                	(select org_name_level_2 from hr_organizations org
                		where org.organization_id = itd.declare_org_id) as don_vi,
                	ii.name as ten_khoan_thu_nhap,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnTongLuong)
                	) as tong_luong,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnLuongChiuThue)
                	) as luong_chiu_thue,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnLuongMienThue)
                	) as luong_mien_thue,      	
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnThuNhapKhac)
                	) as thu_nhap_khac,
                	itd.insurance_deduction as bao_hiem,
                	itd.income_tax thue_ke_khai,
                	itd.month_retro_tax thue_truy_thu_thang,
                	itd.year_retro_tax thue_truy_thu_nam,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnPhucapAC)
                	) as phu_cap_an_ca,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnPhucapDienthoai)
                	) as phu_cap_dien_thoai,
                	(
                		select sum(column_value) from pit_income_item_columns itc
                		where itc.tax_period_date = itd.tax_period_date
                		and itc.income_item_detail_id = itd.income_item_detail_id
                		and itc.column_code in (:columnPhucapKhac)
                	) as phu_cap_khac
                from pit_income_item_masters iim,
                	pit_income_items ii,
                	pit_income_item_details itd
                	left join hr_employees e on itd.emp_code = e.employee_code
                	left join (
                         select pi1.*
                         from hr_personal_identities pi1
                         where pi1.is_main = 'Y'
                         order by pi1.employee_id
                         limit 1
                       ) pi on pi.employee_id = e.employee_id
                where itd.tax_period_date BETWEEN :startDate and :endDate
                and iim.income_item_master_id = itd.income_item_master_id
                and iim.income_item_id = ii.income_item_id
                and iim.is_deleted = 'N'
                and itd.is_deleted = 'N'
                """);
        params.put("startDate", Utils.getFirstDay(reportForm.getStartDate()));
        params.put("endDate", Utils.getLastDay(reportForm.getEndDate()));
        params.put("columnTongLuong", Arrays.asList("LCD", "LT_CT", "LT_MT", "LQ_MC", "LN_MC"));
        params.put("columnThuNhapKhac", Arrays.asList("VL_10", "VL_20", "VL_MT", "TNK_MC", "TLMN"));
        params.put("columnLuongChiuThue", Arrays.asList("LCD", "LT_CT", "LQ_MC", "LN_MC", "VL_10", "VL_20", "VL_MT", "TNK_MC", "TLMN"));
        params.put("columnLuongMienThue", Arrays.asList("LT_MT"));

        params.put("columnPhucapAC", Arrays.asList("PCAC"));
        params.put("columnPhucapDienthoai", Arrays.asList("PCDT"));
        params.put("columnPhucapKhac", Arrays.asList("PCK"));
        if (!Utils.isNullOrEmpty(reportForm.getKeySearch())) {
            sql.append(" and itd.emp_code in (:empCodes)");
            String str = reportForm.getKeySearch().replace(" ", "");
            params.put("empCodes", Arrays.asList(str.split(",")));
        }
        Long unitId = authorizationService.getOrgHasPermission(null, null, null).get(0);
        if (unitId != null) {
            sql.append(" and itd.work_org_id in (select organization_id from hr_organizations o" +
                    " where o.path_id like :permissionUnitIdPath)");
            params.put("permissionUnitIdPath", "%/" + unitId + "/%");
        }
        if(!Utils.isNullOrEmpty(reportForm.getOrgIds())){
            sql.append(" and exists (" +
                    "   select 1 from hr_organizations o, hr_organizations org" +
                    "   where o.organization_id in (:orgIds)" +
                    "   and org.organization_id = itd.work_org_id" +
                    "   and org.path_id like concat('%', o.path_id, '%') " +
                    ")");
            params.put("orgIds", reportForm.getOrgIds());
        }
        return sql.toString();
    }

    public List<Map<String, Object>> getListIncomeByOrg(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.don_vi,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.don_vi
                order by T.don_vi
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListIncomeByIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                order by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListIncomeByAccountingItem(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap,
                    T.khoan_tai_chinh,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap, T.khoan_tai_chinh
                order by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap, T.khoan_tai_chinh
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getListByOrgAndItem(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.don_vi, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.don_vi, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                order by T.don_vi, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> getDependentPersons(RIncomeTaxRequest.ReportByIdForm reportForm) {
        StringBuilder sql = new StringBuilder("""
                select
                    e.employee_code ma_nv,
                    e.full_name ho_ten_nv,
                    org.full_name as don_vi,
                    (select etp.emp_type_id
                        from hr_contract_process etp
                        where etp.employee_id = e.employee_id
                        and etp.is_deleted = 'N'
                        and etp.start_date < :endDate
                        order by etp.start_date desc limit 1)  as doi_tuong,
                    e.tax_no as ma_so_thue_nv,
                    fr.full_name ho_ten_than_nhan,
                    (SELECT sc.name FROM sys_categories sc WHERE sc.value = fr.relation_type_id AND sc.category_type = :relationTypeCode) moi_quan_he,
                    null as ma_so_thue_than_nhan,
                    fr.personal_id_no as so_cccd_than_nhan,
                    null as ngay_sinh_than_nhan,
                    DATE_FORMAT(dp.from_date,'%m/%Y') as tu_ky,
                    DATE_FORMAT(dp.to_date,'%m/%Y') as den_ky,
                    case
                     when not exists (
                        select 1 from pit_income_item_details itd
                        where itd.tax_period_date between :startDate and :endDate
                        and itd.emp_code = e.employee_code
                        and itd.is_deleted = 'N'
                     ) then 'Không phát sinh thu nhập trong kỳ'
                     when e.status = 2 then 'Đã nghỉ việc'
                    end as ghi_chu
                from hr_dependent_persons dp,
                    hr_family_relationships fr,
                    hr_employees e,
                    hr_organizations org
                where dp.is_deleted = :activeStatus
                and fr.is_deleted = 'N'
                and e.is_deleted = 'N'
                and dp.family_relationship_id = fr.family_relationship_id
                and e.employee_id = fr.employee_id
                and dp.tax_organization_id = 1240
                and org.organization_id = e.organization_id
                and dp.from_date <= :endDate
                and IFNULL(dp.to_date, :endDate) >= :startDate
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("relationTypeCode", Constant.CATEGORY_TYPES.MOI_QUAN_HE_TN);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(reportForm.getKeySearch())) {
            sql.append(" and e.employee_code in (:empCodes)");
            String str = reportForm.getKeySearch().replace(" ", "");
            params.put("empCodes", Arrays.asList(str.split(",")));
        }
        if(!Utils.isNullOrEmpty(reportForm.getOrgIds())){
            sql.append(" and exists (" +
                    "   select 1 from hr_organizations o where o.organization_id in (:orgIds)" +
                    "   and org.path_id like concat('%', o.path_id, '%') " +
                    ")");
            params.put("orgIds", reportForm.getOrgIds());
        }
        sql.append(" order by org.path_order");
        params.put("startDate", Utils.getFirstDay(reportForm.getStartDate()));
        params.put("endDate", Utils.getLastDay(reportForm.getEndDate()));
        List result = getListData(sql.toString(), params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return result;
    }

    public List<Map<String, Object>> getListPersonalIncomeByIncomeItem(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.ma_nv, T.ho_va_ten,
                    T.so_cmt, T.ma_so_thue, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.ma_nv, T.ho_va_ten, T.so_cmt, T.ma_so_thue, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                order by ifnull(T.ma_nv,'N/A'), T.ho_va_ten, T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public List<Map<String, Object>> exportIncomeItemForChecking(RIncomeTaxRequest.ReportByIdForm reportForm) {
        String sql = """
                select T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap,
                    {so_lieu_tong_hop}
                from (
                    {cau_lenh_lay_du_lieu}
                ) T
                group by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                order by T.ky_tra, T.ky_luong, T.ten_khoan_thu_nhap
                """;
        Map<String, Object> params = new HashMap<>();
        sql = sql.replace("{so_lieu_tong_hop}", SO_LIEU_TONG_HOP);
        sql = sql.replace("{cau_lenh_lay_du_lieu}", getSQLQuery(reportForm, params));
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }
        return result;
    }

    public String getNameOrganization() {
        String sql = """
                select name from hr_organizations where parent_id is null;
                """;
        Map<String, Object> params = new HashMap<>();
        return queryForObject(sql, params, String.class);
    }
}
