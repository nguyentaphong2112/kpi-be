package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.FeedbackRequest;
import vn.hbtplus.models.response.FeedbackResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FeedbackRepository extends BaseRepository {

    public BaseDataTableDto<FeedbackResponse.SearchResult> searchData(FeedbackRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                select a.feedback_id,
                   a.content,
                   a.status,
                   a.created_time,
                   a.created_by,
                   a.type,
                   (select name from sys_categories sc where sc.value = a.status and sc.category_type = 'SYS_TRANG_THAI_PHAN_ANH') AS status_name,
                   (select name from sys_categories sc where sc.value = a.type and sc.category_type = 'LOAI_PHAN_ANH') AS type_name
                from sys_feedbacks a
                where a.is_deleted = 'N'
                """);
        sql.append(" and a.created_by = :createdBy");
        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", Utils.getUserNameLogin());
        sql.append(" order by a.created_time desc");
        BaseDataTableDto<FeedbackResponse.SearchResult> result = getListPagination(sql.toString(), params, dto, FeedbackResponse.SearchResult.class);
        List<FeedbackResponse.SearchResult> listFeedbacks = result.getListData();
        listFeedbacks.forEach(feedback -> feedback.setComments(getListComment(feedback.getFeedbackId())));

        return result;
    }

    public BaseDataTableDto<FeedbackResponse.SearchResult> adminSearchData(FeedbackRequest.SearchForm dto) {
        Map<String, Object> params = new HashMap<>();
        String sql = initSearchOrExportSql(dto, params);
        BaseDataTableDto<FeedbackResponse.SearchResult> result = getListPagination(sql, params, dto, FeedbackResponse.SearchResult.class);
        List<FeedbackResponse.SearchResult> listFeedbacks = result.getListData();
        listFeedbacks.forEach(feedback -> feedback.setComments(getListComment(feedback.getFeedbackId())));

        return result;
    }

    public List<FeedbackResponse.CommentBean> getListComment(Long feedbackId) {
        String sql = "select a.feedback_comment_id, a.content, " +
                     "  a.created_time, a.created_by " +
                     " from sys_feedback_comments a " +
                     " where a.feedback_id = :feedbackId" +
                     " and a.is_deleted = 'N'" +
                     " order by a.created_time";
        Map<String, Object> params = new HashMap<>();
        params.put("feedbackId", feedbackId);
        return getListData(sql, params, FeedbackResponse.CommentBean.class);
    }

    public FeedbackResponse.DetailBean getDataById(Long feedbackId) {
        String sql = """
                    SELECT
                        a.*,
                        o.full_name orgName,
                        j.name jobName,
                        e.employee_code,
                        e.full_name,
                        u.login_name,
                        (select name from sys_categories sc where sc.value = a.status and sc.category_type = 'SYS_TRANG_THAI_PHAN_ANH') AS statusName,
                        (select name from sys_categories sc where sc.value = a.type and sc.category_type = 'LOAI_PHAN_ANH') AS type_name
                    FROM sys_feedbacks a
                    JOIN sys_users u ON u.user_id = a.user_id
                    JOIN hr_employees e ON e.employee_code = u.employee_code
                    LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                    LEFT JOIN hr_jobs j ON j.job_id = e.job_id
                    WHERE a.is_deleted = 'N'
                    AND a.feedback_id = :feedbackId
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("feedbackId", feedbackId);
        return getFirstData(sql, params, FeedbackResponse.DetailBean.class);
    }

    public List<Map<String, Object>> getListExport(FeedbackRequest.SearchForm dto) {
        Map<String, Object> params = new HashMap<>();
        String sql = initSearchOrExportSql(dto, params);
        return getListData(sql, params);
    }

    private String initSearchOrExportSql(FeedbackRequest.SearchForm dto, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("""
                select a.feedback_id,
                   a.content,
                   a.status,
                   o.full_name orgName,
                   j.name jobName,
                   e.employee_code,
                   e.full_name,
                   a.created_time,
                   DATE_FORMAT(a.created_time, '%d/%m/%Y %H:%i:%s') created_time_str,
                   a.created_by,
                   a.type,
                   (select name from sys_categories sc where sc.value = a.status and sc.category_type = 'SYS_TRANG_THAI_PHAN_ANH') AS status_name,
                   (select name from sys_categories sc where sc.value = a.type and sc.category_type = 'LOAI_PHAN_ANH') AS type_name
                FROM sys_feedbacks a
                JOIN sys_users u ON u.user_id = a.user_id
                JOIN hr_employees e ON e.employee_code = u.employee_code
                LEFT JOIN hr_organizations o ON o.organization_id = e.organization_id
                LEFT JOIN hr_jobs j ON j.job_id = e.job_id
                WHERE a.is_deleted = 'N'
                """);
        QueryUtils.filter(dto.getStatus(), sql, params, "a.status");
        QueryUtils.filterEq(dto.getType(), sql, params, "a.type");
        QueryUtils.filter(dto.getKeySearch(), sql, params, "e.employee_code", "e.full_name", "u.login_name");
        QueryUtils.filterGe(dto.getStartDate(), sql, params, "a.created_time", "startDate");
        QueryUtils.filterLe(dto.getEndDate(), sql, params, "date(a.created_time)", "endDate");

        sql.append(" order by a.created_time desc");

        return sql.toString();
    }
}
