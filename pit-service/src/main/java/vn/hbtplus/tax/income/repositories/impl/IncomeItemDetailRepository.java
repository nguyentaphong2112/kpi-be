package vn.hbtplus.tax.income.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.income.constants.Constant;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.models.IncomeItemColumnDto;
import vn.hbtplus.tax.income.models.IncomeItemDetailsDto;
import vn.hbtplus.tax.income.repositories.entity.IncomeItemMastersEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IncomeItemDetailRepository extends BaseRepository {
    public List<IncomeItemDetailsDto> getDataDetailByMaster(Long incomeItemMasterId) {
        String sql = """
                Select a.*, (
                    select org_name_level_2 from hr_organizations o
                    where o.organization_id = a.declare_org_id
                ) as declare_org_name
                from pit_income_item_details a
                Where a.tax_period_date = :periodDate
                AND ifnull(is_deleted, 'N') = 'N'
                AND income_item_master_id = :incomeItemMasterId
                order by a.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomeItemMasterId", incomeItemMasterId);
        params.put("periodDate", get(IncomeItemMastersEntity.class, incomeItemMasterId).getTaxPeriodDate());

        return getListData(sql, params, IncomeItemDetailsDto.class);
    }

    public Map<String, List<IncomeItemColumnDto>> getIncomeItemColumnByMasterId(Long incomeItemMasterId) {
        String sql = """
                SELECT ic.income_item_master_id, ic.income_item_detail_id, column_value
                FROM pit_income_item_columns ic
                    inner join pit_income_template_columns tc on ic.column_code = tc.column_code
                WHERE income_item_master_id = :incomeItemMasterId and ifnull(ic.is_deleted, 'N') = 'N'
                ORDER BY tc.order_number
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("incomeItemMasterId", incomeItemMasterId);

        List<IncomeItemColumnDto> incomeItemColumnDtoList = getListData(sql, params, IncomeItemColumnDto.class);
        Map<String, List<IncomeItemColumnDto>> mapIncomeItemDetail = new HashMap<>();
        incomeItemColumnDtoList.forEach(item -> {
            String key = String.format(Constant.KEY_MASTER_DETAIL, item.getIncomeItemMasterId(), item.getIncomeItemDetailId());
            List<IncomeItemColumnDto> dtoList;
            if (mapIncomeItemDetail.get(key) == null) {
                dtoList = new ArrayList<>();
            } else {
                dtoList = mapIncomeItemDetail.get(key);
            }
            dtoList.add(item);
            mapIncomeItemDetail.put(key, dtoList);
        });

        return mapIncomeItemDetail;
    }
}
