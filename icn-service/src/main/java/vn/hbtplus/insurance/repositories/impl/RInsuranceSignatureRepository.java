package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.request.InsuranceContributionsRequest;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.SysCategoryEntity;
import vn.hbtplus.utils.Utils;

import java.text.MessageFormat;
import java.util.*;


@Repository
@RequiredArgsConstructor
//@AllArgsConstructor
public class RInsuranceSignatureRepository extends BaseRepository {
    private final RInsuranceContributionsRepository rInsuranceContributionsRepository;
//    @Value("${config.org.support.market:0}")
    private static final Long orgIdSupport = 9004552L;

    public List<Map<String, Object>> getListDetails(InsuranceContributionsRequest.ReportForm dto, String[] types) {
        StringBuilder sql = new StringBuilder("""
                select
                	e.employee_code ma_nv,
                	e.full_name ho_va_ten,
                	a.emp_type_code doi_tuong,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.contract_salary end luong_cap_bac,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.reserve_salary end luong_clbl,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.pos_allowance_salary end pccv,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.seniority_salary end pc_tnvk,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.pos_seniority_salary end pc_tnnghe,
                	case when a.type in (:typeKoQuyLuong) then 0 else a.total_salary end tong_luong,
                	a.per_social_amount as bhxh_ca_nhan,
                	a.unit_social_amount as bhxh_don_vi,
                	a.per_medical_amount as bhyt_ca_nhan,
                	a.unit_medical_amount as bhyt_don_vi,
                	a.per_unemp_amount as bhtn_ca_nhan,
                	a.unit_unemp_amount as bhtn_don_vi,
                	a.total_amount as tong_dong
                 from icn_insurance_contributions a , hr_employees e
                where e.employee_id = a.employee_id
                and a.is_deleted = 'N'
                and a.type in (:typeList)                                
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("typeList", Arrays.asList(types));
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN, InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        CommonRepository.addFilter(sql, params, dto);
        sql.append("""                                
                ORDER BY e.employee_code, a.period_date, a.retro_for_period_date
                """);

        List<Map<String, Object>> result = getListData(sql.toString(), params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql.toString()));
        }

        return result;
    }

    public List<Map<String, Object>> getListSummary(InsuranceContributionsRequest.ReportForm dto, String columnGroup, boolean isKPCD) {
        StringBuilder sql = new StringBuilder("""
                select
                    a.emp_type_code as doi_tuong,
                    (select name from sys_categories sc where sc.value = a.labour_type and sc.category_type = :labourTypeCode) as loai_lao_dong,
                	case when a.type IN (:typeThuBHXH) then a.employee_id else null end  employee_id,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.contract_salary end  luong_cap_bac,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.reserve_salary end  luong_clbl,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.pos_allowance_salary end  pccv,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.seniority_salary end  pc_tnvk,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.pos_seniority_salary end  pc_tnnghe,
                	case when a.type IN (:typeKoQuyLuong) then 0 else a.total_salary end  tong_luong,
                	a.per_social_amount as bhxh_ca_nhan,
                	a.unit_social_amount as bhxh_don_vi,
                	a.per_medical_amount as bhyt_ca_nhan,
                	a.unit_medical_amount as bhyt_don_vi,
                	a.per_unemp_amount as bhtn_ca_nhan,
                	a.unit_unemp_amount as bhtn_don_vi,
                	a.unit_union_amount as tong_kpcd,
                	a.base_union_amount as kpcd_co_so,
                	a.superior_union_amount as kpcd_cap_tren,
                	a.mod_union_amount as kpcd_bqp,
                	a.total_amount as tong_dong
                 from icn_insurance_contributions a 
                where a.is_deleted = :activeStatus
                and a.type IN (:types)    
                """);
        Map<String, Object> params = new HashMap<>();
        if (isKPCD) {
            params.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU_KPCD));
        } else {
            params.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU));
        }
        params.put("typeThuBHXH", Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        params.put("typeKoQuyLuong", Arrays.asList(InsuranceContributionsEntity.TYPES.THAI_SAN, InsuranceContributionsEntity.TYPES.TRUY_THU_BHYT));
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);


        CommonRepository.addFilter(sql, params, dto);

        String queryGroup = MessageFormat.format("""
                Select T.{0}, count(distinct T.employee_id) as so_nguoi,
                Sum(T.luong_cap_bac) luong_cap_bac,
                Sum(T.luong_clbl) luong_clbl,
                Sum(T.pccv) pccv,
                Sum(T.pc_tnvk) pc_tnvk,
                Sum(T.pc_tnnghe) pc_tnnghe,
                Sum(T.tong_luong) tong_luong,
                Sum(T.bhxh_ca_nhan) as bhxh_ca_nhan,
                Sum(T.bhxh_don_vi) as bhxh_don_vi,
                Sum(T.bhyt_ca_nhan) as bhyt_ca_nhan,
                Sum(T.bhyt_don_vi) as bhyt_don_vi,
                Sum(T.bhtn_ca_nhan) as bhtn_ca_nhan,
                Sum(T.bhtn_don_vi) as bhtn_don_vi,
                Sum(T.tong_kpcd) as tong_kpcd,
                Sum(T.kpcd_co_so) as kpcd_co_so,
                Sum(T.kpcd_cap_tren) as kpcd_cap_tren,
                Sum(T.kpcd_bqp) as kpcd_bqp,
                Sum(T.tong_dong) as tong_dong
                """
                + " from (" + sql +
                "  ) T " +
                " group by T.{0}", columnGroup);

        List<Map<String, Object>> listData = getListData(queryGroup, params);
        if (listData.isEmpty()) {
            List<String> aliasColumns = getReturnAliasColumns(queryGroup);
            Map<String, Object> map = new HashMap<>();
            aliasColumns.stream().forEach(item -> {
                map.put(item, null);
            });
            listData.add(map);
        }

        if (!Utils.isNullOrEmpty(listData) && "doi_tuong".equals(columnGroup)) {
            List<SysCategoryEntity> listEmpType = rInsuranceContributionsRepository.getListEmpType();
            List<String> aliasColumns = getReturnAliasColumns(queryGroup);
            Map<String, Object> mapColumn = new HashMap<>();
            aliasColumns.stream().forEach(item -> {
                mapColumn.put(item, null);
            });

            List<Map<String, Object>> listResult = new ArrayList<>();
            for (SysCategoryEntity categoryEntity : listEmpType) {
                boolean flag = true;
                for (Map<String, Object> mapData : listData) {
                    if (mapData.get("doi_tuong").toString().equalsIgnoreCase(categoryEntity.getValue())) {
                        listResult.add(mapData);
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    Map<String, Object> map = new HashMap<>(mapColumn);
                    map.put("doi_tuong", categoryEntity.getValue());
                    listResult.add(map);
                }
            }
            return listResult;
        }
        return listData;

    }

    public List<Map<String, Object>> getDataCompareReport(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                     o.path_order,
                     CASE WHEN o.path_id like '%/:orgIdSupport/%' then o.org_name_level_3 else o.org_name_level_2 end org_name,
                     CASE WHEN a.labour_type = :tt THEN a.per_social_amount ELSE 0 END ttBHXHCaNhan,
                     CASE WHEN a.labour_type = :tt THEN a.unit_social_amount ELSE 0 END ttBHXHDonVi,
                     CASE WHEN a.labour_type = :tt THEN a.per_medical_amount ELSE 0 END ttBHYTCaNhan,
                     CASE WHEN a.labour_type = :tt THEN a.unit_medical_amount ELSE 0 END ttBHYTDonVi,
                     CASE WHEN a.labour_type = :tt THEN a.per_unemp_amount ELSE 0 END ttBHTNCaNhan,
                     CASE WHEN a.labour_type = :tt THEN a.unit_unemp_amount ELSE 0 END ttBHTNDonVi,
                     CASE WHEN a.labour_type = :gt THEN a.per_social_amount ELSE 0 END gtBHXHCaNhan,
                     CASE WHEN a.labour_type = :gt THEN a.unit_social_amount ELSE 0 END gtBHXHDonVi,
                     CASE WHEN a.labour_type = :gt THEN a.per_medical_amount ELSE 0 END gtBHYTCaNhan,
                     CASE WHEN a.labour_type = :gt THEN a.unit_medical_amount ELSE 0 END gtBHYTDonVi,
                     CASE WHEN a.labour_type = :gt THEN a.per_unemp_amount ELSE 0 END gtBHTNCaNhan,
                     CASE WHEN a.labour_type = :gt THEN a.unit_unemp_amount ELSE 0 END gtBHTNDonVi,
                     CASE WHEN a.labour_type = :tt THEN a.unit_union_amount ELSE 0 END ttKPCD,
                     CASE WHEN a.labour_type = :gt THEN a.unit_union_amount ELSE 0 END gtKPCD
                FROM icn_insurance_contributions a
                JOIN hr_organizations o ON o.organization_id = a.debit_org_id
                WHERE a.is_deleted = :activeStatus
                AND a.type IN (:types)
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("types", Arrays.asList(InsuranceContributionsEntity.TYPES.THU));
        params.put("tt", Constant.LABOUR_TYPE.TT);
        params.put("gt", Constant.LABOUR_TYPE.GT);
        CommonRepository.addFilter(sql, params, dto);
        params.put("orgIdSupport", orgIdSupport);

        String sqlSelect = """
                select T.org_name,
                sum(T.ttBHXHCaNhan) ttBHXHCaNhan,
                sum(T.ttBHXHDonVi) ttBHXHDonVi,
                sum(T.ttBHYTCaNhan) ttBHYTCaNhan,
                sum(T.ttBHYTDonVi) ttBHYTDonVi,
                sum(T.ttBHTNCaNhan) ttBHTNCaNhan,
                sum(T.ttBHTNDonVi) ttBHTNDonVi,
                sum(T.gtBHXHCaNhan) gtBHXHCaNhan,
                sum(T.gtBHXHDonVi) gtBHXHDonVi,
                sum(T.gtBHYTCaNhan) gtBHYTCaNhan,
                sum(T.gtBHYTDonVi) gtBHYTDonVi,
                sum(T.gtBHTNCaNhan) gtBHTNCaNhan,
                sum(T.gtBHTNDonVi) gtBHTNDonVi,
                sum(T.ttKPCD) ttKPCD,
                sum(T.gtKPCD) gtKPCD
                 FROM (
                """
                + sql + ") T " +
                " group by T.org_name" +
                " order by T.path_order, T.org_name";

        List<Map<String, Object>> resultList = getListData(sqlSelect, params);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sqlSelect));
        }
        return resultList;

    }

    public Integer getNumOfEmps(InsuranceContributionsRequest.ReportForm dto, String insuranceAgency) {
        StringBuilder sql = new StringBuilder("""
                select
                	count(distinct a.employee_id)
                 from icn_insurance_contributions a , hr_employees e
                where e.employee_id = a.employee_id
                and a.is_deleted = :activeStatus
                and a.type in (:typeList)   
                and a.insurance_agency = :noiThamGiaBHXH 
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("typeList", Arrays.asList(InsuranceContributionsEntity.TYPES.DANH_SACH_CHI_TIET));
        params.put("noiThamGiaBHXH", insuranceAgency);

        CommonRepository.addFilter(sql, params, dto);
        return getFirstData(sql.toString(), params, Integer.class);
    }

    public Map<String, Object> getSummaryForPhieuTrinh(InsuranceContributionsRequest.ReportForm dto) {
        StringBuilder sql = new StringBuilder("""
                select
                	sum(a.per_social_amount)
                	 + sum(per_medical_amount)
                	 + sum(per_unemp_amount) as so_ca_nhan_dong,
                	sum(a.unit_social_amount)
                	 + sum(unit_medical_amount)
                	 + sum(unit_unemp_amount) as so_don_vi_dong,
                	sum(a.per_social_amount)
                	 + sum(per_medical_amount)
                	 + sum(per_unemp_amount) 
                	 + sum(a.unit_social_amount)
                	 + sum(unit_medical_amount)
                	 + sum(unit_unemp_amount) as tong_so_dong
                 from icn_insurance_contributions a 
                where a.is_deleted = :activeStatus
                and a.type <> :type    
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("labourTypeCode", Constant.CATEGORY_TYPE.PHAN_LOAI_LAO_DONG);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("type", InsuranceContributionsEntity.TYPES.KO_THU);

        CommonRepository.addFilter(sql, params, dto);

        List<Map<String, Object>> result = getListData(sql.toString(), params);
        if (result.isEmpty()) {
            List<String> aliasColumns = getReturnAliasColumns(sql.toString());
            Map<String, Object> map = new HashMap<>();
            aliasColumns.stream().forEach(item -> {
                map.put(item, null);
            });
            result.add(map);
        }

        return result.get(0);
    }

}
