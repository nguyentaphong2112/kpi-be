/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Repository;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.request.EmployeesRequest;
import vn.kpi.models.response.WorkProcessResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.WorkProcessEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.QueryUtils;
import vn.kpi.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_work_process
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkProcessRepository extends BaseRepository {
    private final AuthorizationService authorizationService;

    public BaseDataTableDto<WorkProcessResponse.SearchResult> searchData(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        return getListPagination(pair.getLeft(), pair.getRight(), dto, WorkProcessResponse.SearchResult.class);
    }

    private Pair<String, Map<String, Object>> buildSql(EmployeesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.work_process_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.start_date,
                    a.end_date,
                    a.document_type_id,
                    a.job_id,
                    a.position_id,
                    a.organization_id,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    NVL(a.department_name, o.full_name) orgName,
                    case 
                        when mj.job_type = 'CONG_VIEC'
                        then mj.name 
                        else (
                            select mj1.name from hr_concurrent_process cp1, hr_jobs mj1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = a.employee_id
                            and cp1.start_date = a.start_date
                            and mj1.job_type = 'CONG_VIEC'
                            and cp1.is_deleted = 'N'
                            limit 1
                        )
                    end jobName,
                    case 
                        when mj.job_type = 'CHUC_VU'
                        then mj.name 
                        else a.job_title
                    end positionName,
                    (select GROUP_CONCAT(CONCAT_WS(' - ', mj1.name, o1.full_name) SEPARATOR ', ')
                        from hr_concurrent_process cp1, hr_jobs mj1, hr_organizations o1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = a.employee_id
                            and cp1.start_date = a.start_date
                            and o1.organization_id = cp1.organization_id
                            and cp1.is_deleted = 'N'
                    ) as otherPositionName,
                    NVL(a.document_type_name, dt.name) documentTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);

        return new MutablePair<>(sql.toString(), params);
    }

    public List<Map<String, Object>> getListExport(EmployeesRequest.SearchForm dto) {
        Pair<String, Map<String, Object>> pair = buildSql(dto);
        List<Map<String, Object>> dataList = getListData(pair.getLeft(), pair.getRight());
        if (Utils.isNullOrEmpty(dataList)) {
            dataList.add(getMapEmptyAliasColumns(pair.getLeft()));
        }
        return dataList;
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeesRequest.SearchForm dto) {
        sql.append("""
                    FROM hr_work_process a
                    JOIN hr_employees e ON (e.employee_id = a.employee_id AND NVL(e.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                    LEFT JOIN hr_organizations o ON (o.organization_id = a.organization_id AND NVL(o.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN hr_document_types dt ON (dt.document_type_id = a.document_type_id AND NVL(dt.is_deleted, :activeStatus) = :activeStatus)
                    WHERE NVL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        UtilsEmployeeSearchRepository.setParamEmployeeSearch(dto, sql, params);
        QueryUtils.filter(dto.getDocumentNo(), sql, params, "a.document_no");
        if (!Utils.isNullOrEmpty(dto.getListDocumentSignedDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListDocumentSignedDate().get(0)), sql, params, "a.document_signed_date", "fromDocumentSignedDate");
            QueryUtils.filterLe(dto.getListDocumentSignedDate().size() > 1 ? Utils.stringToDate(dto.getListDocumentSignedDate().get(1)) : null, sql, params, "a.document_signed_date", "toDocumentSignedDate");
        }

        if (!Utils.isNullOrEmpty(dto.getListStartDate())) {
            QueryUtils.filterGe(Utils.stringToDate(dto.getListStartDate().get(0)), sql, params, "a.start_date", "startDate");
            QueryUtils.filterLe(dto.getListStartDate().size() > 1 ? Utils.stringToDate(dto.getListStartDate().get(1)) : null, sql, params, "a.start_date", "endDate");
        }
        if (!Utils.isNullOrEmpty(dto.getListDocumentType())) {
            sql.append(" and a.document_type_id in (:documentTypeIds)");
            params.put("documentTypeIds", dto.getListDocumentType());
        }

        List<PermissionDataDto> permissionDataDtos = authorizationService.getPermissionData(
                Scope.VIEW, Constant.RESOURCES.WORK_PROCESS, Utils.getUserNameLogin()
        );
        sql.append(" and (a.department_name is not null or (1=1 ");
        QueryUtils.addConditionPermission(permissionDataDtos, sql, params);
        sql.append(" ))");

        sql.append(" ORDER BY e.employee_id, a.start_date DESC");
    }

    public boolean isConflictProcess(Date startDate, Long employeeId, Long workProcessId) {
        String sql = """
                    SELECT count(1)
                    FROM hr_work_process sp
                    WHERE sp.employee_id = :employeeId
                    AND sp.work_process_id != :workProcessId
                    AND sp.start_date = :startDate
                    AND NVL(sp.is_deleted, :activeStatus) = :activeStatus
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("workProcessId", Utils.NVL(workProcessId, 0L));
        params.put("startDate", startDate);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, params, Integer.class) > 0;
    }

    public WorkProcessEntity getPreProcess(Long employeeId, Date startDate) {
        String sql = """
                    select * from hr_work_process
                    WHERE employee_id = :employeeId
                    AND NVL(is_deleted, :activeStatus) = :activeStatus
                    and start_date < :startDate
                    order by start_date desc limit 1
                """;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("employeeId", employeeId);
        paramMap.put("startDate", startDate);
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return queryForObject(sql, paramMap, WorkProcessEntity.class);
    }

    public void updateWorkProcess(Long employeeId, String updateBy) {
        String sql = """
                    update
                    hr_work_process wp ,
                        (
                            select
                                wp1.work_process_id,
                                (select min(wp2.start_date) from hr_work_process wp2
                                    where wp2.employee_id = wp1.employee_id
                                    and wp2.start_date > wp1.start_date
                                    and wp2.is_deleted = :activeStatus
                                ) as next_start_date
                            from hr_work_process wp1
                            where wp1.is_deleted = :activeStatus
                        ) T
                    set wp.end_date = DATE_ADD(T.next_start_date,INTERVAL -1 DAY)
                        ,wp.modified_by = :userName
                        ,wp.modified_time = now()
                    where wp.work_process_id = T.work_process_id
                    and ifnull(wp.end_date,now()) <> ifnull(DATE_ADD(T.next_start_date,INTERVAL -1 DAY),now())
                    and wp.is_deleted = :activeStatus
                    and wp.employee_id = :employeeId
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("userName", updateBy);
        executeSqlDatabase(sql, params);
        updateEmpInfoByWorkProcess(employeeId, null, null);
    }


    public BaseDataTableDto<WorkProcessResponse.DetailBean> getTableList(Long employeeId, BaseSearchRequest request) {
        String sql = """
                SELECT
                    e.employee_id,
                    a.work_process_id,
                    a.start_date,
                    a.end_date,
                    a.document_type_id,
                    a.job_id,
                    a.position_id,
                    a.organization_id,
                    a.document_no,
                    a.document_signed_date,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    NVL(a.department_name, o.full_name) orgName,
                    case 
                        when mj.job_type = 'CONG_VIEC'
                        then mj.name 
                        else (
                            select mj1.name from hr_concurrent_process cp1, hr_jobs mj1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = a.employee_id
                            and cp1.start_date = a.start_date
                            and mj1.job_type = 'CONG_VIEC'
                            and cp1.is_deleted = 'N'
                            limit 1
                        )
                    end jobName,
                    case 
                        when mj.job_type = 'CHUC_VU'
                        then mj.name 
                        else a.job_title
                    end positionName,
                    (select GROUP_CONCAT(CONCAT_WS(' - ', mj1.name, o1.full_name) SEPARATOR ', ')
                        from hr_concurrent_process cp1, hr_jobs mj1, hr_organizations o1
                            where cp1.job_id = mj1.job_id 
                            and cp1.employee_id = a.employee_id
                            and cp1.start_date = a.start_date
                            and o1.organization_id = cp1.organization_id
                            and cp1.is_deleted = 'N'
                    ) as otherPositionName,
                    NVL(dt.name, a.document_type_name) documentTypeName,
                    (select sc.name from sys_categories sc where sc.value = e.status and sc.category_type = :empStatus) empStatusName,
                    (select et.name from hr_emp_types et where et.emp_type_id = e.emp_type_id) empTypeName
                FROM hr_work_process a
                JOIN hr_employees e ON e.employee_id = a.employee_id
                LEFT JOIN hr_jobs mj ON mj.job_id = a.job_id
                LEFT JOIN hr_organizations o ON o.organization_id = a.organization_id
                LEFT JOIN hr_document_types dt ON dt.document_type_id = a.document_type_id
                WHERE a.is_deleted = :activeStatus
                and a.employee_id = :employeeId
                order by a.start_date desc
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("employeeId", employeeId);
        params.put("empStatus", Constant.CATEGORY_CODES.HR_TRANG_THAI_NHAN_VIEN);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, params, request, WorkProcessResponse.DetailBean.class);
    }

    public void updateEmpInfoByWorkProcess(Long employeeId, Date fromDate, String modifiedBy) {
        String sql = """
                UPDATE hr_employees e
                   JOIN (
                   SELECT
                       wp.employee_id,wp.position_id,
                       wp.job_id,wp.job_title,wp.department_name,
                       wp.organization_id,
                       ifnull((
                           SELECT attribute_value
                           FROM hr_object_attributes ot
                           WHERE ot.object_id = dt.document_type_id
                             AND ot.table_name = 'hr_document_types'
                             AND ot.attribute_code = 'TRANG_THAI_NHAN_VIEN'
                                        and ot.is_deleted = 'N'
                           LIMIT 1
                       ),'1') AS status,
                       CASE
                           WHEN DATE(NOW()) BETWEEN wp.start_date AND IFNULL(wp.end_date, NOW())
                           THEN 1
                       END AS is_current,
                       ROW_NUMBER() OVER (
                           PARTITION BY wp.employee_id
                           ORDER BY wp.start_date DESC
                       ) AS row_num
                   FROM hr_work_process wp
                   LEFT JOIN hr_document_types dt  ON wp.document_type_id = dt.document_type_id
                   WHERE wp.is_deleted = 'N' {append_sql_filter_emp}
                   ) wp ON wp.employee_id = e.employee_id and IFNULL(wp.is_current,wp.row_num) = 1
                   SET e.position_id     = wp.position_id,
                       e.organization_id = wp.organization_id,
                       e.job_title       = wp.job_title,
                       e.department_name = wp.department_name,
                       e.job_id          = wp.job_id,
                       e.status          = wp.status
                   where 1=1
                """;
        if (employeeId != null) {
            sql = sql.replace("{append_sql_filter_emp}", " and wp.employee_id = :employeeId");
            sql = sql + " and e.employee_id = :employeeId";
        }
        if (fromDate != null) {
            sql += " and exists (" +
                   "    select 1 from hr_work_process wp1" +
                   "    where wp1.from_date >= :fromDate" +
                   "    and wp1.employee_id = e.employee_id" +
                   ")";
        }
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("modifiedBy", modifiedBy);
        if (employeeId != null) {
            paramMap.put("employeeId", employeeId);
        }
        if (fromDate != null) {
            paramMap.put("fromDate", fromDate);
        }
        executeSqlDatabase(sql, paramMap);
    }

    public WorkProcessEntity getLeastWorkProcess(Long employeeId) {
        String sql = """
                select wp.* from hr_work_process wp
                where wp.is_deleted = 'N'
                and wp.employee_id = :employeeId
                order by wp.start_date desc
                limit 1
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("employeeId", employeeId);
        return queryForObject(sql, paramMap, WorkProcessEntity.class);
    }

    public void updateConcurrentProcess(Long employeeId, String userName) {
        String sql = """
                update hr_concurrent_process cp, hr_work_process wp
                    set cp.end_date = wp.end_date, cp.modified_time = now(), cp.modified_by = :userName
                where cp.employee_id = wp.employee_id
                and cp.start_date = wp.start_date
                and cp.is_deleted = 'N'
                and wp.is_deleted = 'N'
                and wp.employee_id = :employeeId
                """;
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        paramMap.put("employeeId", employeeId);
        paramMap.put("userName", userName);
        executeSqlDatabase(sql, paramMap);
    }
}
