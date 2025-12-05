package vn.hbtplus.tax.income.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.repositories.entity.TaxRateDetailEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TaxRateDetailRepository extends BaseRepository {

    public List<TaxRateDetailEntity> getTaxRatios(Date taxPeriodDate) {
        String sql = "select a.* from pit_tax_rate_details a," +
                "   pit_tax_rates b" +
                "   where a.tax_rate_id = b.tax_rate_id" +
                "   and :taxPeriodDate between b.start_date and ifnull(b.end_date, :taxPeriodDate)" +
                "   and a.is_deleted = 'N'" +
                "   and b.is_deleted = 'N'" +
                "   order by a.amount desc";
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("taxPeriodDate", taxPeriodDate);
        return getListData(sql, mapParams, TaxRateDetailEntity.class);
    }
}
