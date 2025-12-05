/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.InsuranceContributionsDto;
import vn.hbtplus.insurance.models.request.InsuranceRetractionsRequest;
import vn.hbtplus.insurance.models.response.InsuranceRetractionsResponse;
import vn.hbtplus.insurance.repositories.entity.InsuranceContributionsEntity;
import vn.hbtplus.insurance.repositories.entity.InsuranceRetractionsEntity;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang icn_insurance_retractions
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class InsuranceRetractionsRepository extends BaseRepository {

    /**
     * tim kiem tai popup truy thu truy linh
     *
     * @param dto tham so tim kiem
     * @return danh sach
     */
    public BaseDataTableDto searchDataPopup(InsuranceRetractionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.insurance_retraction_id,
                    a.period_date,
                    e.employee_code,
                    e.full_name,
                    a.contract_salary,
                    a.reserve_salary,
                    a.pos_allowance_salary,
                    a.seniority_salary,
                    a.pos_seniority_salary,
                    a.per_social_amount,
                    a.unit_social_amount,
                    a.per_medical_amount,
                    a.unit_medical_amount,
                    a.per_unemp_amount,
                    a.unit_unemp_amount,
                    a.total_amount,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) empTypeName,
                    a.labour_type,
                    mj.name job_name,
                    o.name org_name,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                FROM icn_insurance_retractions a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                JOIN hr_organizations o ON o.organization_id = a.org_id
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                and a.table_type in (:tableTypes)
                AND a.retro_period_date IS NULL
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("tableTypes", InsuranceRetractionsEntity.TABLE_TYPES.CHENH_LECH);

        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name", "e.email");
        QueryUtils.filter(dto.getListType(), sql, params, "a.type");
        QueryUtils.filterGe(Utils.getFirstDay(dto.getFromPeriodDate()), sql, params, "a.period_date", "fromDate");
        QueryUtils.filterLe(Utils.getLastDay(dto.getToPeriodDate()), sql, params, "a.period_date", "toDate");

        sql.append(" ORDER BY o.path_order, e.employee_code, a.period_date");
        return getListPagination(sql.toString(), params, dto, InsuranceRetractionsResponse.class);
    }

    public BaseDataTableDto searchData(InsuranceRetractionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.insurance_retraction_id,
                    a.period_date,
                    a.retro_period_date,
                    e.employee_code,
                    e.full_name,
                    a.contract_salary,
                    a.reserve_salary,
                    a.pos_allowance_salary,
                    a.seniority_salary,
                    a.pos_seniority_salary,
                    a.per_social_amount,
                    a.unit_social_amount,
                    a.per_medical_amount,
                    a.unit_medical_amount,
                    a.per_unemp_amount,
                    a.unit_unemp_amount,
                    a.total_amount,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) empTypeName,
                    a.labour_type,
                    mj.name job_name,
                    a.reason,
                    o.name orgName,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto, false);
        return getListPagination(sql.toString(), params, dto, InsuranceRetractionsResponse.class);
    }

    public int deleteByForm(InsuranceRetractionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                    DELETE a FROM icn_insurance_retractions a
                    WHERE NOT EXISTS(
                            select 1 from icn_insurance_contributions ic
                            where ic.insurance_contribution_id = a.insurance_contribution_id
                            and ic.is_deleted = :activeStatus
                            and ic.status = :statusApprove
                    )
                
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("statusApprove", InsuranceContributionsEntity.STATUS.PHE_DUYET);
        if (!Utils.isNullObject(dto.getListOrgId())) {
            sql.append("""
                    AND EXISTS(
                        select 1 from hr_organizations o
                        where o.organization_id = a.org_id
                    
                    """);
            sql.append(" AND ( 1 = 0 ");
            for (Long orgId : dto.getListOrgId()) {
                sql.append(" OR o.path_id LIKE :path" + orgId);
                params.put("path" + orgId, "%/" + orgId + "/%");
            }
            sql.append(")");
            sql.append(")");
        }

        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append("""
                    AND EXISTS(
                        select 1 from hr_employees e
                        where e.employee_id = a.employee_id
                        and (
                            e.employee_code like :keySearch
                            or e.full_name like :keySearch
                            or e.email like :keySearch
                        )
                    )
                    
                    """);
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }

        QueryUtils.filter(Utils.getLastDay(dto.getPeriodDate()), sql, params, "a.period_date");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "a.emp_type_code");
        QueryUtils.filter(dto.getListType(), sql, params, "a.type");
        if (!Utils.isNullOrEmpty(dto.getListStatus()) && dto.getListStatus().size() == 1) {
            if ("1".equals(dto.getStatus())) {
                sql.append(" AND a.retro_period_date IS NOT NULL");
            } else {
                sql.append(" AND a.retro_period_date IS NULL");
            }
        }
        return executeSqlDatabase(sql.toString(), params);

    }

    public List<Map<String, Object>> getListExport(InsuranceRetractionsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.period_date,
                    e.employee_code,
                    e.full_name,
                    a.contract_salary,
                    a.reserve_salary,
                    a.pos_allowance_salary,
                    a.seniority_salary,
                    a.pos_seniority_salary,
                    a.per_social_amount,
                    a.unit_social_amount,
                    a.per_medical_amount,
                    a.unit_medical_amount,
                    a.per_unemp_amount,
                    a.unit_unemp_amount,
                    a.total_amount,
                    after.contract_salary after_contract_salary,
                    after.reserve_salary after_reserve_salary,
                    after.pos_allowance_salary after_pos_allowance_salary,
                    after.seniority_salary after_seniority_salary,
                    after.pos_seniority_salary after_pos_seniority_salary,
                    after.per_social_amount after_per_social_amount,
                    after.unit_social_amount after_unit_social_amount,
                    after.per_medical_amount after_per_medical_amount,
                    after.unit_medical_amount after_unit_medical_amount,
                    after.per_unemp_amount after_per_unemp_amount,
                    after.unit_unemp_amount after_unit_unemp_amount,
                    after.total_amount after_total_amount,
                    pre.contract_salary pre_contract_salary,
                    pre.reserve_salary pre_reserve_salary,
                    pre.pos_allowance_salary pre_pos_allowance_salary,
                    pre.seniority_salary pre_seniority_salary,
                    pre.pos_seniority_salary pre_pos_seniority_salary,
                    pre.per_social_amount pre_per_social_amount,
                    pre.unit_social_amount pre_unit_social_amount,
                    pre.per_medical_amount pre_per_medical_amount,
                    pre.unit_medical_amount pre_unit_medical_amount,
                    pre.per_unemp_amount pre_per_unemp_amount,
                    pre.unit_unemp_amount pre_unit_unemp_amount,
                    pre.total_amount pre_total_amount,
                    (select et.name from hr_emp_types et where et.code = a.emp_type_code) empTypeName,
                    a.labour_type,
                    mj.name job_name,
                    a.reason as ly_do,
                    date_format(a.period_date, '%m/%Y') as ky_trich_nop,
                    date_format(a.retro_period_date, '%m/%Y') as ky_thuc_hien,
                    o.org_name_level_2 don_vi,
                    o.org_name_level_3 phong_ban
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto, true);
        List results = getListData(sql.toString(), params);
        if (results.isEmpty()) {
            results.add(getMapEmptyAliasColumns(sql.toString()));
        }
        return results;
    }

    private void addCondition(StringBuilder sql,
                              Map<String, Object> params,
                              InsuranceRetractionsRequest.SearchForm dto,
                              boolean isExport) {
        sql.append("""
                    FROM icn_insurance_retractions a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.org_id
                """);
        if (isExport) {
            sql.append(" LEFT JOIN icn_insurance_retractions after on a.insurance_retraction_id = after.base_id and after.table_type = 'PHAI_THU'");
            sql.append(" LEFT JOIN icn_insurance_retractions pre on a.insurance_retraction_id = pre.base_id and pre.table_type = 'DA_THU'");
        }
        sql.append("WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus" +
                   "                    and a.table_type = :tableType");
        params.put("tableType", InsuranceRetractionsEntity.TABLE_TYPES.CHENH_LECH);

        if (!Utils.isNullOrEmpty(dto.getListOrgId())) {
            sql.append(" AND ( 1 = 0 ");
            for (Long orgId : dto.getListOrgId()) {
                sql.append(" OR o.path_id LIKE :path" + orgId);
                params.put("path" + orgId, "%/" + orgId + "/%");
            }
            sql.append(")");
        }
        QueryUtils.filterOrg(dto.getOrgId(), sql, params, "o.path_id");
        QueryUtils.filter(Utils.getLastDay(dto.getPeriodDate()), sql, params, "a.period_date");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name", "e.email");
        QueryUtils.filter(dto.getListEmpTypeCode(), sql, params, "a.emp_type_code");
        QueryUtils.filter(dto.getListType(), sql, params, "a.type");
        if (!Utils.isNullOrEmpty(dto.getListStatus()) && dto.getListStatus().size() == 1) {
            if ("1".equals(dto.getListStatus().get(0))) {
                sql.append(" AND a.retro_period_date IS NOT NULL");
            } else {
                sql.append(" AND a.retro_period_date IS NULL");
            }
        }

        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        sql.append(" ORDER BY a.period_date desc, o.path_order, e.employee_code");
    }

    public boolean existsNotApproved(Date periodDate) {
        String sql = """
                select 1 from icn_insurance_contributions ic
                where ic.period_date = :periodDate
                and ic.is_deleted = 'N'
                and ic.status not in (:statusApproved)
                limit 1
                """;
        HashMap<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);
        map.put("statusApproved", List.of(InsuranceContributionsEntity.STATUS.PHE_DUYET));
        return getFirstData(sql, map, Long.class) != null;
    }

    public List<InsuranceContributionsDto> getExistsContributions(List<String> empCodes, Date periodDate) {
        String sql = """
                 select * from icn_insurance_contributions ic
                where ic.period_date = :periodDate
                and ic.is_deleted = 'N'
                and ic.type in (:type)
                and ic.status in (:statusApproved)
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);
        map.put("statusApproved", Collections.singletonList(InsuranceContributionsEntity.STATUS.PHE_DUYET));
        map.put("type", Collections.singletonList(InsuranceContributionsEntity.TYPES.THU_BHXH));
        if (empCodes != null && !empCodes.isEmpty()) {
            sql += " and ic.employee_id in (select employee_id from hr_employees e where e.employee_code in (:empCodes))";
            map.put("empCodes", empCodes);
        }
        return getListData(sql, map, InsuranceContributionsDto.class);
    }

    public void deleteOldData(List<String> empCodes, Date periodDate, List<Long> keepEmpIds) {
        String sql = "delete a from icn_insurance_retractions a" +
                     " where a.period_date = :periodDate" +
                     "   and a.retro_period_date is null" +
                     "   and a.table_type = 'CHENH_LECH'";
        Map<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);

        if (empCodes != null && !empCodes.isEmpty()) {
            sql += " and a.employee_id in (select employee_id from hr_employees e where e.employee_code in (:empCodes))";
            map.put("empCodes", empCodes);
        }
        if (keepEmpIds != null && !keepEmpIds.isEmpty()) {
            sql += " and a.employee_id not in (:keepEmpIds)";
            map.put("keepEmpIds", keepEmpIds);
        }
        executeSqlDatabase(sql, map);

        //xoa du lieu ban ghi DA_THU, PHAI_THU
        sql = """
                DELETE FROM icn_insurance_retractions
                WHERE period_date = :periodDate
                AND table_type IN (:tableTypes) 
                AND base_id NOT IN (
                    SELECT tmp.insurance_retraction_id
                    FROM (
                        SELECT b.insurance_retraction_id
                        FROM icn_insurance_retractions b
                        WHERE b.period_date = :periodDate
                    ) AS tmp
                )
                """;
        map.put("tableTypes", Arrays.asList(InsuranceRetractionsEntity.TABLE_TYPES.DA_THU, InsuranceRetractionsEntity.TABLE_TYPES.PHAI_THU));
        executeSqlDatabase(sql, map);
    }

    public void saveAll(List<InsuranceContributionsDto> contributionsDtos, Date periodDate) throws BaseAppException {
        List<InsuranceRetractionsEntity> insuranceContributionsEntities = new ArrayList<>();
        String userName = Utils.getUserNameLogin();
        Set<String> positionErrors = new HashSet<>();
        Long startId = getNextId(InsuranceRetractionsEntity.class, contributionsDtos.size() * 3);
        for (InsuranceContributionsDto item : contributionsDtos) {
            InsuranceRetractionsEntity entity = new InsuranceRetractionsEntity(startId, periodDate,
                    InsuranceRetractionsEntity.TABLE_TYPES.CHENH_LECH,
                    userName, item);
            entity.setReason(item.getLyDoTruyThuTruyLinh());
            insuranceContributionsEntities.add(entity);
            if (Utils.isNullOrEmpty(item.getLabourType())) {
                positionErrors.add(item.getJobName());
            }
            if (item.getSoDaThuDto() != null) {
                entity = new InsuranceRetractionsEntity(startId + 1, periodDate,
                        InsuranceRetractionsEntity.TABLE_TYPES.DA_THU,
                        userName, item.getSoDaThuDto());
                entity.setBaseId(startId);
                insuranceContributionsEntities.add(entity);
            }
            if (item.getSoPhaiThuDto() != null) {
                entity = new InsuranceRetractionsEntity(startId + 2, periodDate,
                        InsuranceRetractionsEntity.TABLE_TYPES.PHAI_THU,
                        userName, item.getSoPhaiThuDto());
                entity.setBaseId(startId);
                insuranceContributionsEntities.add(entity);
            }
            startId = startId + 3;
        }
        insertBatch(InsuranceRetractionsEntity.class, insuranceContributionsEntities, userName);
    }

    public List<InsuranceContributionsDto> getExistsRetro(List<String> empCodes, Date periodDate) {
        String sql = """
                 select * from icn_insurance_retractions ic
                where ic.period_date = :periodDate
                and ic.is_deleted = 'N'
                and ic.table_type = 'CHENH_LECH'
                and ic.retro_period_date is not null
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);
        if (empCodes != null && !empCodes.isEmpty()) {
            sql += " and ic.employee_id in (select employee_id from hr_employees e where e.employee_code in (:empCodes))";
            map.put("empCodes", empCodes);
        }
        return getListData(sql, map, InsuranceContributionsDto.class);
    }

    public List<InsuranceContributionsDto> getListOldRetro(List<String> empCodes, Date periodDate) {
        String sql = """
                 select * from icn_insurance_retractions ic
                where ic.period_date = :periodDate
                and ic.is_deleted = 'N'
                and ic.table_type = 'CHENH_LECH'
                and ic.retro_period_date is null
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);
        if (empCodes != null && !empCodes.isEmpty()) {
            sql += " and ic.employee_id in (select employee_id from hr_employees e where e.employee_code in (:empCodes))";
            map.put("empCodes", empCodes);
        }
        return getListData(sql, map, InsuranceContributionsDto.class);
    }

    public boolean existsApproved(Date periodDate) {
        String sql = """
                select 1 from icn_insurance_contributions ic
                where ic.period_date = :periodDate
                and ic.is_deleted = 'N'
                and ic.status in (:statusApproved)
                limit 1
                """;
        HashMap<String, Object> map = new HashMap<>();
        map.put("periodDate", periodDate);
        map.put("statusApproved", Arrays.asList(new String[]{InsuranceContributionsEntity.STATUS.PHE_DUYET}));
        return getFirstData(sql, map, Long.class) != null;
    }
}
