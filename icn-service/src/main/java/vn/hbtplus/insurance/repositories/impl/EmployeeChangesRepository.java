/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.insurance.models.request.EmployeeChangesRequest;
import vn.hbtplus.insurance.models.response.EmployeeChangesResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.EmployeeChangesEntity;
import vn.hbtplus.insurance.repositories.jpa.ContributionRateRepositoryJPA;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.*;

/**
 * Lop repository Impl ung voi bang icn_employee_changes
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeeChangesRepository extends BaseRepository {
    private final ContributionRateRepositoryJPA contributionRateRepositoryJPA;
    private final ConfigParameterRepository configParameterRepository;

    public BaseDataTableDto searchData(EmployeeChangesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_change_id,
                    a.period_date,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.change_date,
                    a.change_type,
                    (select s.name from sys_categories s where s.value = a.change_type and s.category_type = :changeType) changeTypeName,
                    a.contribution_type,
                    (select s.name from sys_categories s where s.value = a.contribution_type and s.category_type = :contributionType) contributionTypeName,
                    a.reason,
                    a.organization_id,
                    o.name orgName,
                    a.job_id,
                    mj.name jobName,
                    a.status,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :status) statusName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeeChangesResponse.class);
    }

    public List<Map<String, Object>> getListExport(EmployeeChangesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_change_id,
                    a.period_date,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.change_date,
                    a.change_type,
                    (select s.name from sys_categories s where s.value = a.change_type and s.category_type = :changeType) changeTypeName,
                    a.contribution_type,
                    (select s.name from sys_categories s where s.value = a.contribution_type and s.category_type = :contributionType) contributionTypeName,
                    a.reason,
                    a.organization_id,
                    o.name org_name,
                    a.job_id,
                    mj.name job_name,
                    a.status,
                    (select s.name from sys_categories s where s.value = a.status and s.category_type = :status) statusName,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeeChangesRequest.SearchForm dto) {
        sql.append("""
                    FROM icn_employee_changes a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    JOIN hr_organizations o ON o.organization_id = a.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("changeType", Constant.CATEGORY_TYPE.LOAI_THAY_DOI);
        params.put("contributionType", Constant.CATEGORY_TYPE.TRANG_THAI_TRICH_NOP);
        params.put("status", Constant.CATEGORY_TYPE.TRANG_THAI_RA_SOAT_DU_LIEU);
        QueryUtils.filterLikeOrg(dto.getListOrgId(), sql, params, "o.path_id");
        if (dto.getPeriodDate() != null) {
            sql.append(" and a.period_date = :periodDate");
            params.put("periodDate", Utils.getLastDay(dto.getPeriodDate()));
        }
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name", "e.email");
        QueryUtils.filterEq(dto.getChangeType(), sql, params, "a.change_type");
        QueryUtils.filterEq(dto.getContributionType(), sql, params, "a.contribution_type");
        QueryUtils.filterEq(dto.getStatus(), sql, params, "a.status");
    }

    public List<EmployeeChangesEntity> getListDataByForm(EmployeeChangesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("SELECT a.*");
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params, EmployeeChangesEntity.class);
    }

    public void updateStatus(List<Long> ids, String status) {
        if (Utils.isNullOrEmpty(ids)) {
            return;
        }
        String sql = "update icn_employee_changes a" +
                " set a.status = :status," +
                "   a.modified_by = :userName," +
                "   a.modified_time = :currentDate" +
                " where a.employee_change_id in (:ids)";
        final String userName = Utils.getUserNameLogin();
        final Date currentDate = new Date();
        Map map = new HashMap();
        map.put("userName", userName);
        map.put("status", status);
        map.put("currentDate", currentDate);
        map.put("ids", ids);
        executeSqlDatabase(sql, map);
    }

    public List<EmployeeChangesEntity> getListEmployeeChange(Date periodDate) {
        String sql = """
                select
                	T.employee_id,
                	T.start_date as change_date,
                	T.organization_id,
                	T.job_id,
                	T.change_type,
                	T.loai as reason
                from
                (select 
                	e.employee_id,
                	e.employee_code,
                	e.full_name,
                	wp.start_date,
                	wp.organization_id,
                	wp.job_id,
                	'CHUYEN_DEN' as change_type,
                	CONCAT('Điều chuyển về từ ' , org1.org_name_level_1) loai
                from hr_work_process wp,
                hr_employees e,
                hr_document_types dt,
                hr_document_types dt1,
                hr_work_process wp1,
                hr_organizations org1,
                hr_organizations org
                where wp.document_type_id = dt.document_type_id
                and wp1.document_type_id = dt1.document_type_id
                and wp.start_date BETWEEN :startDate and :endDate
                and wp1.employee_id = wp.employee_id
                and wp.employee_id = e.employee_id
                and wp1.organization_id = org1.organization_id
                and wp.organization_id = org.organization_id
                and wp1.end_date = DATE_ADD(wp.start_date,INTERVAL -1 day)
                and dt.type <> 'OUT'
                and dt1.type <> 'OUT'
                and org.path_id like :orgVCCPath
                and org1.path_id not like :orgVCCPath
                union all
                select  e.employee_id,
                	e.employee_code,
                	e.full_name,
                	wp.start_date,
                	wp1.organization_id,
                	wp1.job_id,
                	'CHUYEN_DI' as change_type,
                	CONCAT('Điều chuyển sang ' , org.org_name_level_1) loai 
                from hr_work_process wp,
                hr_employees e,
                hr_document_types dt,
                hr_document_types dt1,
                hr_work_process wp1,
                hr_organizations org1,
                hr_organizations org
                where wp.document_type_id = dt.document_type_id
                and wp1.document_type_id = dt1.document_type_id
                and wp.start_date BETWEEN :startDate and :endDate
                and wp1.employee_id = wp.employee_id
                and wp.employee_id = e.employee_id
                and wp1.organization_id = org1.organization_id
                and wp.organization_id = org.organization_id
                and wp1.end_date = DATE_ADD(wp.start_date,INTERVAL -1 day)
                and dt.type <> 'OUT'
                and dt1.type <> 'OUT'
                and org.path_id not like :orgVCCPath
                and org1.path_id like :orgVCCPath
                union all
                select  e.employee_id,
                	e.employee_code,
                	e.full_name,
                	etp.start_date ,
                	wp.organization_id,
                	wp.job_id,
                	'HDLD_MOI' as change_type,
                	CONCAT('Mới ký HĐLĐ từ ngày ', DATE_FORMAT(etp.start_date,'%d/%m/%Y')) loai
                from hr_contract_process etp, hr_work_process wp, hr_employees e,
                hr_document_types dt,hr_organizations org, hr_emp_types ets
                where etp.start_date BETWEEN :startDate and :endDate
                and wp.document_type_id = dt.document_type_id
                and wp.employee_id = etp.employee_id
                and ets.emp_type_id = etp.emp_type_id
                and ets.code in (:empTypeCodes)
                and not exists (
                	select 1 from hr_contract_process etp1, hr_emp_types ets1
                	where etp1.employee_id = etp.employee_id
                	and etp1.end_date = DATE_ADD(etp.start_date,INTERVAL -1 day)
                	and ets1.emp_type_id = etp1.emp_type_id
                    and ets1.code in (:empTypeCodes)
                )
                and dt.type <> 'OUT'
                and wp.employee_id = e.employee_id
                and etp.start_date BETWEEN wp.start_date and IFNULL(wp.end_date,:endDate)
                and wp.organization_id = org.organization_id
                and org.path_id like :orgVCCPath
                union all
                select  e.employee_id,
                	e.employee_code,
                	e.full_name,
                	etp.start_date ,
                	wp.organization_id,
                	wp.job_id,
                	'NGHI_VIEC' as change_type,
                	CONCAT('Nghỉ việc từ ngày ', DATE_FORMAT(wp.start_date,'%d/%m/%Y')) loai
                from hr_contract_process etp, hr_work_process wp, hr_employees e,
                hr_document_types dt,hr_organizations org, hr_emp_types ets
                where wp.start_date BETWEEN :startDate and :endDate
                and wp.document_type_id = dt.document_type_id
                and wp.employee_id = etp.employee_id
                and ets.emp_type_id = etp.emp_type_id
                and ets.code in (:empTypeCodes)
                and dt.type = 'OUT'
                and wp.employee_id = e.employee_id
                and DATE_ADD(wp.start_date,INTERVAL -1 day) BETWEEN etp.start_date and IFNULL(etp.end_date,:endDate)
                and wp.organization_id = org.organization_id
                and org.path_id like :orgVCCPath) T
                where not exists (
                    select 1 from icn_employee_changes a
                    where a.is_deleted = 'N'
                    and a.employee_id = T.employee_id
                    and a.period_date = :endDate
                    and a.status = 'PHE_DUYET'
                )
                """;
        Map params = new HashMap();
        params.put("orgVCCPath", "%/" + configParameterRepository.getConfigValue(Constant.CONFIG_PARAMETERS.ROOT_LEGACY_ID, periodDate, Long.class) + "/%");
        params.put("startDate", DateUtils.addDays(Utils.getFirstDay(periodDate), 1));
        params.put("endDate", Utils.getLastDay(periodDate));
        params.put("empTypeCodes", contributionRateRepositoryJPA.getListEmTypeCodes(periodDate));

        return getListData(sql, params, EmployeeChangesEntity.class);
    }

    public void insertOrUpdate(List<EmployeeChangesEntity> list, Date periodDate) {
        String userName = Utils.getUserNameLogin();
        String sqlUpdate = """
                update icn_employee_changes a
                    set a.change_date = :change_date,
                    a.change_type = :change_type,
                    a.contribution_type = :contribution_type,
                    a.reason = :reason,
                    a.organization_id = :organization_id,
                    a.job_id = :job_id,
                    a.status = :status,
                    a.is_deleted = 'N'
                where a.employee_id = :employee_id
                and a.period_date = :period_date
                """;
        String sqlInsert = "insert into icn_employee_changes(period_date, employee_id, change_date, change_type," +
                           " contribution_type, reason, organization_id, job_id, status, is_deleted, created_by, created_time)" +
                           " select :period_date, :employee_id, :change_date, :change_type, " +
                           " :contribution_type, :reason, :organization_id, :job_id, :status," +
                           " 'N', :userName, now()" +
                           " from dual " +
                           " where not exists (" +
                           "    select 1 from icn_employee_changes a" +
                           "    where a.is_deleted = 'N'" +
                           "    and a.employee_id = :employee_id" +
                           "    and a.period_date = :period_date" +
                           " )";

        List<Map> listParams = new ArrayList<>();
        list.forEach(item -> {
            Map map = new HashMap<>();
            map.put("change_date", item.getChangeDate());
            map.put("change_type", item.getChangeType());
            map.put("contribution_type", item.getContributionType());
            map.put("reason", item.getReason());
            map.put("organization_id", item.getOrganizationId());
            map.put("job_id", item.getJobId());
            map.put("employee_id", item.getEmployeeId());
            map.put("period_date", periodDate);
            map.put("userName", userName);
            map.put("status", "CHO_PHE_DUYET");
            listParams.add(map);
        });
        executeBatch(sqlUpdate, listParams);
        executeBatch(sqlInsert, listParams);
    }

    public List<EmployeeChangesEntity> getListEntities(Date periodDate) {
        String sql = "SELECT a.* from icn_employee_changes a" +
                     " where a.is_deleted = 'N'" +
                     " and a.period_date = :periodDate";
        Map mapParams = new HashMap();
        mapParams.put("periodDate", periodDate);
        return getListData(sql, mapParams, EmployeeChangesEntity.class);
    }
}
