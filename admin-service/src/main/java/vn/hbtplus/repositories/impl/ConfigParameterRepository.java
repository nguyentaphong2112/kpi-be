package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.ParameterRequest;
import vn.hbtplus.models.response.ConfigParameterResponse;
import vn.hbtplus.models.response.ParameterResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.ConfigParameterEntity;
import vn.hbtplus.repositories.entity.ParameterEntity;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ConfigParameterRepository extends BaseRepository {

    public BaseDataTableDto<ParameterResponse.SearchResult> search(ParameterRequest.SearchForm request, String configGroup) {
        StringBuilder sql = new StringBuilder("select a.config_group," +
                " a.start_date," +
                " cp.config_group_name," +
                " cp.config_period_type," +
                " max(a.end_date) end_date," +
                " max(a.created_by) created_by," +
                " max(a.created_time) created_time," +
                " max(a.modified_by) modified_by," +
                " max(a.modified_time) modified_time");
        HashMap<String, Object> params = new HashMap<>();
        addCondition(request, params, sql);
        params.put("configGroup", configGroup);
        return getListPagination(sql.toString(), params, request, ParameterResponse.SearchResult.class);
    }

    private void addCondition(ParameterRequest.SearchForm request, HashMap<String, Object> params, StringBuilder sql) {
        sql.append(" from sys_parameters a " +
                "   join sys_config_parameters cp on a.config_group = cp.config_group" +
                "   where a.is_deleted = 'N'" +
                "   and a.config_group = :configGroup" +
                "   group by a.config_group, a.start_date " +
                "   having 1 = 1 "
        );

        if (request.getStartDate() != null) {
            sql.append(" and NVL(max(a.end_date), :startDate) >= :startDate ");
            params.put("startDate", request.getStartDate());
        }

        if (request.getEndDate() != null) {
            sql.append(" and NVL(a.start_date, :endDate) <= :endDate ");
            params.put("endDate", request.getEndDate());
        }
        if (!Utils.isNullOrEmpty(request.getIsNotExpired()) && request.getIsNotExpired().equals("true")) {
            sql.append("""
                    
                    and CURRENT_DATE > a.start_date AND (max(a.end_date) IS NULL OR CURRENT_DATE < max(a.end_date))
                    
                    """);
        }
        sql.append(" order by a.start_date");
    }

    public ConfigParameterResponse getConfigGroup(String configGroup) {
        String sql = "select a.* from sys_config_parameters a" +
                " where a.is_deleted = 'N'" +
                "   and a.config_group = :configGroup";
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("configGroup", configGroup);
        return getFirstData(sql, mapParams, ConfigParameterResponse.class);

    }

    public ParameterEntity getConflict(ParameterRequest.SubmitForm request, String configGroup, Date startDate) {
        String sql = "select a.* from sys_parameters a" +
                " where a.is_deleted = 'N'" +
                (startDate == null ? "" : "   and a.start_date <> :oldStartDate") +
                "   and a.config_group = :configGroup" +
                "   and a.config_code in (:codes)" +
                "   and a.start_date >= :startDate" +
                (request.getEndDate() == null ? "" : " and a.start_date <= :endDate");
        Map mapParams = new HashMap();
        mapParams.put("startDate", request.getStartDate());
        mapParams.put("configGroup", configGroup);

        mapParams.put("codes", request.getColumns()
                .stream()
                .map(ParameterRequest.SubmitForm.Column::getConfigCode)
                .collect(Collectors.toList()));
        if (startDate != null) {
            mapParams.put("oldStartDate", startDate);
        }
        if (request.getEndDate() != null) {
            mapParams.put("endDate", request.getEndDate());
        }
        return getFirstData(sql, mapParams, ParameterEntity.class);

    }


    public List<ConfigParameterEntity> getConfigGroups(String moduleCode) {
        StringBuilder sql = new StringBuilder("""
                select a.* from sys_config_parameters a
                where a.is_deleted = 'N' 
                """);
        Map<String, Object> map = new HashMap<>();
        if (!Utils.isNullOrEmpty(moduleCode)) {
            sql.append(" and a.module_code = :moduleCode");
            map.put("moduleCode", moduleCode);
        }
        sql.append(" order by a.order_number, a.config_group_name");
        return getListData(sql.toString(), map, ConfigParameterEntity.class);
    }

    public Map<String, String> getParameters(List<String> configCodes) {
        String sql = """
                select a.* from sys_parameters a 
                where a.is_deleted = 'N'
                and a.start_date <= now()
                and ifnull(a.end_date, now()) >= DATE(now())
                and a.config_code in (:codes)
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("codes", configCodes);
        List<ParameterResponse.ColumnResponse> results = getListData(sql, map, ParameterResponse.ColumnResponse.class);
        Map mapResults = new HashMap();
        results.forEach(item -> {
            mapResults.put(item.getConfigCode(), item.getConfigValue());
        });
        return mapResults;
    }

    public List<ConfigParameterResponse> getListConfigByCodes(List<String> codes) {
        String sql = "select * from sys_config_parameters a" +
                " where a.is_deleted = :activeStatus" +
                " and a.config_group in (:codes)" +
                " order by a.config_group_name";
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("codes", codes);
        return getListData(sql, params, ConfigParameterResponse.class);
    }

    public ConfigParameterResponse getParameter(String code, String codeGroup) {
        String sql = """
                select a.* from sys_parameters a 
                where a.is_deleted = 'N'
                and a.start_date <= now()
                and ifnull(a.end_date, now()) >= DATE(now())
                and a.config_code = :code
                and a.config_group = :codeGroup
                LIMIT 1
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("codes", code);
        params.put("codeGroup", codeGroup);
        return queryForObject(sql, params, ConfigParameterResponse.class);
    }

    public ConfigParameterEntity getByConfigGroup(String configGroup) {
        String sql = """
                select a.* from sys_config_parameters a
                where a.is_deleted = 'N'
                and a.config_group = :configGroup
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("configGroup", configGroup);
        return getFirstData(sql, params, ConfigParameterEntity.class);
    }

}
