package vn.hbtplus.tax.income.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.response.IncomeTemplatesResponse;
import vn.hbtplus.tax.income.repositories.entity.IncomeTemplateColumnsEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IncomeTemplateRepository extends BaseRepository {
    public List<IncomeTemplatesResponse> getAll() {
        String sql = """
                 select * from pit_income_templates
                 where ifnull(is_deleted, :isDelete) = :isDelete
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDelete", BaseConstants.STATUS.NOT_DELETED);
        return getListData(sql, params, IncomeTemplatesResponse.class);
    }

    public Map<String, List<String>> getMapIncomeTypeColumns() {
        String sql = "select itc.column_code, itc.income_type " +
                "   from pit_income_template_columns itc " +
                " where itc.is_deleted = 'N'";
        Map<String, Object> params = new HashMap<>();
        Map<String, List<String>> mapResults = new HashMap<>();
        List<IncomeTemplateColumnsEntity> listColumns = getListData(sql, params, IncomeTemplateColumnsEntity.class);
        listColumns.stream().forEach(item -> {
            if (mapResults.get(item.getIncomeType()) == null) {
                mapResults.put(item.getIncomeType(), new ArrayList<>());
            }
            if (!mapResults.get(item.getIncomeType()).contains(item.getColumnCode())) {
                mapResults.get(item.getIncomeType()).add(item.getColumnCode());
            }
        });
        return mapResults;
    }
}
