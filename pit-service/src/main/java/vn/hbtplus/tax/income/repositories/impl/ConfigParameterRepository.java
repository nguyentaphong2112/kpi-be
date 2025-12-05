package vn.hbtplus.tax.income.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.annotations.Parameter;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.tax.income.models.request.ParameterRequest;
import vn.hbtplus.tax.income.models.response.ConfigParameterResponse;
import vn.hbtplus.tax.income.models.response.ParameterResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.tax.income.repositories.entity.ConfigParameterEntity;
import vn.hbtplus.tax.income.repositories.entity.ParameterEntity;
import vn.hbtplus.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ConfigParameterRepository extends BaseRepository {

    public BaseDataTableDto<ParameterResponse> search(ParameterRequest.SearchForm request, String configGroup) {
        StringBuilder sql = new StringBuilder("select a.config_group," +
                " a.start_date," +
                " a.end_date end_date," +
                " max(a.created_by) created_by," +
                " max(a.created_time) created_time," +
                " max(a.modified_by) modified_by," +
                " max(a.modified_time) modified_time ");
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, request);
        params.put("configGroup", configGroup);
        return getListPagination(sql.toString(), params, request, ParameterResponse.class);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ParameterRequest.SearchForm request) {
        sql.append("""
                from sys_parameters a 
                where a.is_deleted = 'N' and a.config_group = :configGroup 
                """);
        if (request.getStartDate() != null) {
            Date startDate = Utils.getFirstDay(request.getStartDate());
            sql.append(" and (a.end_date is null or a.end_date >= :startDate) ");
            params.put("startDate", startDate);
        }

        if (request.getEndDate() != null) {
            Date endDate = Utils.getLastDay(request.getEndDate());
            sql.append(" and a.start_date <= :endDate ");
            params.put("endDate", endDate);
        }
        sql.append("""
                group by a.config_group, a.start_date
                order by a.start_date
                """);

    }

    public ConfigParameterResponse getConfigGroup(String configGroup) {
        String sql = "select a.* from icn_config_parameters a" +
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

    public List<ConfigParameterEntity> getConfigGroups(String moduleCode) {
        StringBuilder sql = new StringBuilder("""
                select a.* from icn_config_parameters a
                where a.is_deleted = 'N' 
                """);
        Map<String, Object> map = new HashMap<>();
        if(!Utils.isNullOrEmpty(moduleCode)){
            sql.append(" and a.module_code = :moduleCode");
            map.put("moduleCode", moduleCode);
        }
        sql.append(" order by a.order_number, a.config_group_name");
        return getListData(sql.toString(), map, ConfigParameterEntity.class);
    }
}
