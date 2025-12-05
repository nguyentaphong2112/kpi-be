package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.LogTaskRequest;
import vn.hbtplus.models.response.LogTasKResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Repository
public class LogTaskRepository extends BaseRepository {
    public BaseDataTableDto<LogTasKResponse.SearchResult> searchData(LogTaskRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.log_task_id,
                    a.name,
                    a.is_deleted,
                    a.log_date,
                    a.description,
                    a.total_house,
                    (select e.full_name from hr_employees e where e.employee_code = a.created_by) createdBy,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    (select sc.name from sys_categories sc where sc.value = a.project_code and sc.category_type = :projectCode) projectCode
                    FROM log_tasks a
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        HashMap<String, Object> params = new HashMap<>();
        params.put("projectCode", Constant.CATEGORY_CODES.HRM_LOG_TASK);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getProjectCode(), sql, params, "a.project_code");
        QueryUtils.filter(dto.getName(), sql, params, "a.name");
        QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.log_date", "fromDate");
        QueryUtils.filterLe(dto.getToDate(), sql, params, "a.log_date", "toDate");
        sql.append(" ORDER BY a.log_task_id");
        return getListPagination(sql.toString(), params, dto, LogTasKResponse.SearchResult.class);
    }

    public List<LogTasKResponse.UnloggedUser> findUnloggedUsersToday() {
        String sql = """
                SELECT DISTINCT
                    e.employee_code,
                    e.telegram_chat_id
                FROM hr_employees e
                WHERE e.telegram_chat_id IS NOT NULL
                  AND e.is_deleted = :activeStatus
                  AND NOT EXISTS (
                      SELECT 1
                      FROM log_tasks a
                      WHERE a.created_by = e.employee_code
                        AND a.log_date = :today
                        AND a.is_deleted = :activeStatus
                  )
                """;

        HashMap<String, Object> params = new HashMap<>();
        params.put("today", LocalDate.now());
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, LogTasKResponse.UnloggedUser.class);
    }
}
