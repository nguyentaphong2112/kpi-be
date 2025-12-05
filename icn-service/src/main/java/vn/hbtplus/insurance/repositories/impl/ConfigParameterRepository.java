package vn.hbtplus.insurance.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.annotations.Parameter;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.ParameterEntity;
import vn.hbtplus.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ConfigParameterRepository extends BaseRepository {

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

    private List<ParameterEntity> getListParameters(List<String> codes, Date date) {
        String sql = "select a.* from sys_parameters a where a.is_deleted = 'N'" +
                "   and upper(a.config_code) in (:codes)" +
                "   and a.start_date <= :date " +
                "   and (a.end_date is null or a.end_date >= :date)";
        Map<String, Object> map = new HashMap<>();
        map.put("codes", codes);
        map.put("date", date);
        return getListData(sql, map, ParameterEntity.class);
    }

    public <T> T getConfig(Class<T> className, Date date)
            throws InstantiationException, IllegalAccessException, BaseAppException {
        List<String> codes = new ArrayList<>();
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(Parameter.class) != null) {
                codes.add(field.getAnnotation(Parameter.class).code().toUpperCase());
            }
        }
        List<ParameterEntity> entities = getListParameters(codes, date);
        Map<String, String> mapValues = new HashMap<>();
        entities.stream().forEach(item -> {
            mapValues.put(item.getConfigCode().toUpperCase(), item.getConfigValue());
        });
        //to init T with value mapValues
        T configObject = className.newInstance();

        // Iterate through the fields and set their values from mapValues
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            Parameter parameter = field.getAnnotation(Parameter.class);
            if (parameter != null) {
                String value = mapValues.get(parameter.code().toUpperCase());
                if (!Utils.isNullOrEmpty(value)) {
                    Class<?> fieldType = field.getType();
                    if ("java.lang.String".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, value);
                    } else if ("java.lang.Double".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Double.valueOf(value));
                    } else if ("java.lang.Long".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Long.valueOf(value));
                    } else if ("java.lang.Integer".equalsIgnoreCase(fieldType.getName())) {
                        field.set(configObject, Integer.valueOf(value));
                    } else if (fieldType.isAssignableFrom(List.class) && field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        if (genericType.equals(Long.class)) {
                            List<Long> longList = Arrays.stream(value.split(","))
                                    .map(Long::valueOf)
                                    .collect(Collectors.toList());
                            field.set(configObject, longList);
                        } else if (genericType.equals(String.class)) {
                            value = value.replace(" ", "");
                            field.set(configObject, Arrays.asList(value.split(",")));
                        }
                    }
                } else {
                    if (Utils.isNullOrEmpty(parameter.name())) {
                        throw new BaseAppException("Chưa có dữ liệu cấu hình của " + field.getAnnotation(Parameter.class).code());

                    } else {
                        throw new BaseAppException("Chưa có dữ liệu cấu hình của " + field.getAnnotation(Parameter.class).name());

                    }
                }
            }
        }

        return configObject;
    }
}
