package vn.hbtplus.repositories;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ParameterRepository {
    List<Map<String, Object>> getListParameters(List<String> codes, Date date);

    <T> T getConfigValue(String configCode, Date date, Class<T> className);
}
