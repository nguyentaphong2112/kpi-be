package vn.hbtplus.insurance.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.insurance.models.request.ContributionRateRequest;
import vn.hbtplus.insurance.models.response.ContributionRateResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.ContributionRateEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ContributionRateRepository extends BaseRepository {

    public BaseDataTableDto<ContributionRateResponse> search(ContributionRateRequest.SearchForm request) {
        StringBuilder sql = new StringBuilder("select a.contribution_rate_id," +
                " a.emp_type_code," +
                " a.unit_social_percent," +
                " a.per_social_percent," +
                " a.unit_medical_percent," +
                " a.per_medical_percent," +
                " a.unit_unemp_percent," +
                " a.per_unemp_percent," +
                " a.unit_union_percent," +
                " a.per_union_percent," +
                " a.start_date," +
                " a.end_date," +
                " a.created_by," +
                " a.created_time," +
                " a.modified_by," +
                " a.modified_time");
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, request);
        return getListPagination(sql.toString(), params, request, ContributionRateResponse.class);
    }

    private void addCondition(StringBuilder sql, HashMap<String, Object> params, ContributionRateRequest.SearchForm request) {
        sql.append(" from icn_contribution_rates a where a.is_deleted = 'N' ");
        QueryUtils.filter(request.getEmpTypeCode(), sql, params, "a.emp_type_code");
        QueryUtils.filterGe(Utils.getFirstDay(request.getStartDate()), sql, params, "a.end_date", "startDate");
        QueryUtils.filterLe(Utils.getLastDay(request.getEndDate()), sql, params, "a.start_date", "endDate");
    }

    public ContributionRateEntity getConflict(ContributionRateRequest.SubmitForm request, Long id) {
        String sql = "select a.* from icn_contribution_rates a" +
                " where a.is_deleted = 'N'" +
                "   and a.contribution_rate_id <> :id" +
                "   and a.emp_type_code = :empTypeCode" +
                "   and a.start_date >= :startDate" +
                (request.getEndDate() == null ? "" : " and a.start_date <= :endDate");
        Map mapParams = new HashMap();
        mapParams.put("id", Utils.NVL(id));
        mapParams.put("startDate", request.getStartDate());
        mapParams.put("empTypeCode", request.getEmpTypeCode());
        if (request.getEndDate() != null) {
            mapParams.put("endDate", request.getEndDate());
        }
        return getFirstData(sql, mapParams, ContributionRateEntity.class);

    }
}
