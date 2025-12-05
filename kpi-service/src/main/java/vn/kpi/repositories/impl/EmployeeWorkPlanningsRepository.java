/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.models.request.EmployeeWorkPlanningsRequest;
import vn.kpi.models.response.EmployeeWorkPlanningsResponse;
import vn.kpi.constants.BaseConstants;
import vn.kpi.repositories.entity.EmployeeWorkPlanningsEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang kpi_employee_work_plannings
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class EmployeeWorkPlanningsRepository extends BaseRepository {

    public BaseDataTableDto searchData(EmployeeWorkPlanningsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_work_planning_id,
                    a.employee_evaluation_id,
                    a.content,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_deleted,
                    a.name,
                    a.order_number
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, EmployeeWorkPlanningsResponse.SearchForm.class);
    }

    public List<Map<String, Object>> getListExport(EmployeeWorkPlanningsRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.employee_work_planning_id,
                    a.employee_evaluation_id,
                    a.content,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.is_deleted,
                    a.name,
                    a.order_number
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, EmployeeWorkPlanningsRequest.SearchForm dto) {
        sql.append("""
            FROM kpi_employee_work_plannings a
            
            
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<EmployeeWorkPlanningsEntity> getListEmployeeWorkPlanning(Long id) {
        String sql = """
                SELECT
                    a.*,
                    b.adjust_reason
                    from kpi_employee_work_plannings a
                    JOIN kpi_employee_evaluations b ON a.employee_evaluation_id = b.employee_evaluation_id
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                    and a.employee_evaluation_id = :id
                    ORDER BY a.order_number
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("id", id);
        return getListData(sql, params, EmployeeWorkPlanningsEntity.class);

    }
}
