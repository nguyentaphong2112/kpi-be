package vn.hbtplus.tax.personal.services.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.ParameterRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ParameterRepositoryImpl extends BaseRepository implements ParameterRepository {
    @Override
    public List<Map<String, Object>> getListParameters(List<String> codes, Date date) {
        String sql = "select a.config_code, a.config_value " +
                "   from sys_parameters a where a.is_deleted = 'N'" +
                "   and upper(a.config_code) in (:codes)" +
                "   and a.start_date <= :date " +
                "   and (a.end_date is null or a.end_date >= :date)";
        Map<String, Object> map = new HashMap<>();
        map.put("codes", codes);
        map.put("date", date);
        return getListData(sql, map);
    }

    @Override
    public <T> T getConfigValue(String configCode, Date date, Class<T> className) {
        String sql = "select a.config_value from sys_parameters a " +
                " where upper(a.config_code) = :configCode" +
                "   and a.is_deleted = 'N'" +
                "   and a.start_date <= :date" +
                "   and (a.end_date is null or a.end_date >= :date)";
        Map<String, Object> map = new HashMap<>();
        map.put("configCode", configCode);
        map.put("date", date);
        return getFirstData(sql, map, className);
    }
}
