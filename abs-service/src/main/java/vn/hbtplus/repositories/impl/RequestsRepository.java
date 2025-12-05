/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.AbsTimekeepingDTO;
import vn.hbtplus.models.request.RequestsRequest;
import vn.hbtplus.models.response.RequestsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.RequestsEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang abs_requests
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class RequestsRepository extends BaseRepository {

    public BaseDataTableDto searchData(RequestsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.request_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    a.status,
                    (select name from sys_categories sc1
                        where sc1.category_type = 'ABS_TRANG_THAI_DON'
                        and sc1.code = a.status) statusName,
                    a.start_time,
                    a.end_time,
                    a.reason_type_id,
                    et.name as emp_type_name,
                    mj.name as job_name,
                    o.full_name as organization_name,
                    (SELECT art.name FROM abs_reason_types art where art.reason_type_id =  a.reason_type_id) reasonTypeName,
                    a.note,
                    a.request_no,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, RequestsResponse.class);
    }

    public List<Map<String, Object>> getListExport(RequestsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.request_id,
                    a.employee_id,
                    e.employee_code,
                    e.full_name,
                    e.email,
                    a.status,
                    CASE
                        WHEN a.status = 'DU_THAO' THEN 'Dự thảo'
                        WHEN a.status = 'CHO_PHE_DUYET' THEN 'Chờ phê duyệt'
                        WHEN a.status = 'DA_PHE_DUYET' THEN 'Đã phê duyệt'
                        WHEN a.status = 'DA_HUY' THEN 'Đã hủy'
                        WHEN a.status = 'DA_TU_CHOI' THEN 'Đã từ chối'
                        ELSE 'Không xác định'
                    END AS statusName,
                    a.start_time,
                    a.end_time,
                    a.reason_type_id,
                    (SELECT art.name FROM abs_reason_types art where art.reason_type_id =  a.reason_type_id) reasonTypeName,
                    a.note,
                    a.request_no,
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

    private void addCondition(StringBuilder sql, Map<String, Object> params, RequestsRequest.SearchForm dto) {
        sql.append("""
                    FROM abs_requests a
                    JOIN hr_employees e ON e.employee_id = a.employee_id
                    JOIN hr_emp_types et ON et.emp_type_id =  e.emp_type_id
                    LEFT JOIN hr_jobs mj ON mj.job_id = e.job_id
                    JOIN hr_organizations o ON o.organization_id = e.organization_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        QueryUtils.filter(dto.getListStatus(), sql, params, "a.status");
        QueryUtils.filter(dto.getRequestNo(), sql, params, "a.request_no");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name");
        QueryUtils.filter(dto.getReasonTypeId(), sql, params, "a.reason_type_id");

        if (dto.getListEmpTypeId() != null && !dto.getListEmpTypeId().isEmpty()) {
            sql.append(" and e.emp_type_id in (:listEmpTypeId)");
            params.put("listEmpTypeId", dto.getListEmpTypeId());
        }

        if (!Utils.isNullObject(dto.getOrganizationId())) {
            sql.append(" AND o.path_id LIKE :orgId ");
            params.put("orgId", "%/" + dto.getOrganizationId() + "/%");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null) {
            if (dto.getStartTime().equals(dto.getEndTime())) {
                sql.append(" AND a.start_time = :startOfDay AND a.end_time = :endOfDay");
                params.put("startOfDay", dto.getStartTime());
                params.put("endOfDay", dto.getStartTime());
            } else {
                sql.append(" AND a.start_time >= :startTime AND a.end_time <= :endTime");
                params.put("startTime", dto.getStartTime());
                params.put("endTime", dto.getEndTime());
            }
        } else if (dto.getStartTime() != null) {
            sql.append(" AND a.start_time >= :startTime");
            params.put("startTime", dto.getStartTime());
        } else if (dto.getEndTime() != null) {
            sql.append(" AND a.end_time <= :endTime");
            params.put("endTime", dto.getEndTime());
        }

    }

    public List<RequestsEntity> getListData() {
        StringBuilder sql = new StringBuilder("""
                SELECT
                     a.*
                FROM abs_requests a
                WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                AND a.status = :status
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("status", Constant.REQUEST_STATUS.WAIT_APPROVE);
        return getListData(sql.toString(), params, RequestsEntity.class);
    }

    public List<AbsTimekeepingDTO> getListRequestChange(Date lastRun) {
        String sql = """
                select 
                	cd.calendar_date as dateTimekeeping,
                	rq.employee_id
                from sys_calendars cd, abs_requests rq
                where cd.calendar_date BETWEEN DATE(rq.start_time) and rq.end_time
                and ifnull(rq.last_update_time, rq.created_time) >= DATE_ADD(:lastRunTime, interval -5 MINUTE)
                """;
        Map<String, Object> params = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        params.put("lastRunTime", lastRun == null ? Utils.stringToDate("01/01" + cal.get(Calendar.YEAR)) : lastRun);
        return getListData(sql, params, AbsTimekeepingDTO.class);
    }
}
