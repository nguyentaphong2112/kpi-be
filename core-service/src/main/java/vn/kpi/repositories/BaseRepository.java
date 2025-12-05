package vn.kpi.repositories;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import vn.kpi.annotations.Attribute;
import vn.kpi.constants.BaseConstants;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.BaseSearchRequest;
import vn.kpi.models.dto.BaseCategoryDto;
import vn.kpi.utils.Utils;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class BaseRepository {

    @Autowired
    @Qualifier("primaryJdbc")
    protected NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier("secondJdbc")
    protected NamedParameterJdbcTemplate secondJdbcTemplate;

    protected String springDataSource;

    private NamedParameterJdbcTemplate getJdbcTemplate() {
        return "datasource2".equalsIgnoreCase(springDataSource) ? secondJdbcTemplate : jdbcTemplate;
    }

    public <T> List<T> findAll(Class<T> className) {
        try {
            String sql = "SELECT * FROM " + getSQLTableName(className)
                         + " WHERE is_deleted = 'N'";
            Map<String, Object> mapParam = new HashMap<>();
            return getJdbcTemplate().query(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }


    public <T> List<T> findAllByProperties(Class<T> className, Object... pairs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE 1 = 1 ");
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = convertObjectNameToSqlName((String) obj);
                        if (pairs.length - 1 == index) {//cot chan
                            orderNumber = fieldName;
                        } else {
                            tempFieldName = "fieldName" + index;
                            sql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }
            if (!Utils.isNullOrEmpty(orderNumber)) {
                sql.append(" ORDER BY ").append(orderNumber);
            }
            return getJdbcTemplate().query(sql.toString(), mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> Stream<T> getDataToStream(String sql, Map<String, Object> params, Class<T> classOf) {
        try {
            return getJdbcTemplate().queryForStream(sql, params, new BeanPropertyRowMapper<>(classOf));
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
    }

    public <T> List<T> findByProperties(Class<T> className, List<Pair<String, Object>> pairList, String order) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE 1 = 1 ");
            Map<String, Object> mapParam = new HashMap<>();
            if (!Utils.isNullOrEmpty(pairList)) {
                for (Pair<String, Object> pair : pairList) {
                    String fieldName = convertObjectNameToSqlName(pair.getLeft());
                    Object value = pair.getRight();
                    if (value instanceof List<?>) {
                        List<Object> valueList = (List<Object>) value;
                        if (Utils.isNullOrEmpty(valueList)) {
                            return new ArrayList<>();
                        }
                        sql.append(" AND ").append(fieldName).append(" IN (:").append(pair.getLeft().trim() + ")");
                    } else {
                        sql.append(" AND ").append(fieldName).append(" = :").append(pair.getLeft().trim());
                    }
                    mapParam.put(pair.getLeft(), pair.getRight());
                }
            }
            if (StringUtils.isNotBlank(order)) {
                sql.append(" ORDER BY ").append(order);
            }
            return getJdbcTemplate().query(sql.toString(), mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> List<T> findByProperties(Class<T> className, Object... pairs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE 1 = 1");
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = convertObjectNameToSqlName((String) obj);
                        if (pairs.length - 1 == index) {//cot chan
                            orderNumber = fieldName;
                        } else {
                            tempFieldName = "fieldName" + index;
                            sql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }

            sql.append(" AND is_deleted = 'N'");
            if (!Utils.isNullOrEmpty(orderNumber)) {
                sql.append(" ORDER BY ").append(orderNumber);
            }
            return getJdbcTemplate().query(sql.toString(), mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> List<Map<String, Object>> getListMapObjectByProperties(Class<T> className, Object... pairs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE 1 = 1");
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = convertObjectNameToSqlName((String) obj);
                        if (pairs.length - 1 == index) {//cot chan
                            orderNumber = fieldName;
                        } else {
                            tempFieldName = "fieldName" + index;
                            sql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }

            sql.append(" AND is_deleted = 'N'");
            if (!Utils.isNullOrEmpty(orderNumber)) {
                sql.append(" ORDER BY ").append(orderNumber);
            }
            return getListData(sql.toString(), mapParam);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> List<T> findByListId(Class<T> className, List<Long> listId) {
        if (listId == null || listId.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String idColumnName = getIdColumnName(className);
            String tableName = getSQLTableName(className);
            String sql = "SELECT * FROM " + tableName
                         + " WHERE " + idColumnName + " IN (:ids)";
            Map<String, Object> mapParam = new HashMap<>();
            List<List<Long>> listPartition = Utils.partition(listId, 999);
            List<T> result = new ArrayList<>();
            for (List<Long> ids : listPartition) {
                mapParam.put("ids", ids);
                result.addAll(getJdbcTemplate().query(sql, mapParam, new BeanPropertyRowMapper(className)));
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> List<T> findByListObject(Class<T> className, String columnName, List listId) {
        if (listId == null || listId.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String tableName = getSQLTableName(className);
            String sql = "SELECT * FROM " + tableName
                         + " WHERE " + convertObjectNameToSqlName(columnName) + " IN(:ids)";
            Map<String, Object> mapParam = new HashMap<>();
            List<List<Long>> listPartition = Utils.partition(listId, 999);
            List<T> result = new ArrayList<>();
            for (List<Long> ids : listPartition) {
                mapParam.put("ids", ids);
                result.addAll(getJdbcTemplate().query(sql, mapParam, new BeanPropertyRowMapper(className)));
            }
            return result;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> T get(Class<T> className, Object columnValue) {
        try {
            String idColumnName = getIdColumnName(className);
            String tableName = getSQLTableName(className);
            String sql = "SELECT * FROM " + tableName
                         + " WHERE " + idColumnName + " = :columnValue";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("columnValue", columnValue);
            return (T) getJdbcTemplate().queryForObject(sql, mapParam, new BeanPropertyRowMapper(className));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T get(Class<T> className, Object... pairs) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE 1 = 1");
            Map<String, Object> mapParam = new HashMap<>();
            String orderNumber = "";
            if (pairs != null) {
                int index = 0;
                String tempFieldName = "";
                for (Object obj : pairs) {
                    if (index % 2 == 0) {
                        String fieldName = convertObjectNameToSqlName((String) obj);
                        if (pairs.length - 1 == index) {//cot chan
                            orderNumber = fieldName;
                        } else {
                            tempFieldName = "fieldName" + index;
                            sql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                        }
                    } else {
                        mapParam.put(tempFieldName, obj);
                    }
                    index++;
                }
            }

            sql.append(" AND is_deleted = 'N'");
            if (!Utils.isNullOrEmpty(orderNumber)) {
                sql.append(" ORDER BY ").append(orderNumber);
            }
            List<T> result = getJdbcTemplate().query(sql.toString(), mapParam, new BeanPropertyRowMapper(className));
            if (result == null || result.isEmpty()) {
                return null;
            } else {
                return result.get(0);
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Integer count(Class className, Object columnValue) {
        try {
            String idColumnName = getIdColumnName(className);
            String tableName = getSQLTableName(className);
            String sql = "SELECT COUNT(1) FROM " + tableName
                         + " WHERE " + idColumnName + " = :columnValue " +
                         "   AND is_deleted = 'N'";
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("columnValue", columnValue);
            return getJdbcTemplate().queryForObject(sql, mapParam, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public boolean duplicate(Class className, Long idValue, Object... pairs) {
        StringBuilder hql = new StringBuilder(" SELECT COUNT(*) FROM "
                                              + getSQLTableName(className)
                                              + " WHERE is_deleted = 'N'");
        Map<String, Object> mapParams = new HashMap<>();
        if (idValue != null && idValue > 0L) {
            hql.append(" AND ").append(getIdColumnName(className)).append(" != :idValue ");
            mapParams.put("idValue", idValue);
        }
        if (pairs != null) {
            int index = 0;
            String tempFieldName = "";
            for (Object obj : pairs) {
                if (index % 2 == 0) {
                    String fieldName = convertObjectNameToSqlName((String) obj);
                    tempFieldName = "fieldName" + index;
                    hql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                } else {
                    mapParams.put(tempFieldName, obj);
                }
                index++;
            }
        }
        Long count = getJdbcTemplate().queryForObject(hql.toString(), mapParams, Long.class);
        return count > 0;
    }

    public int deleteByProperties(Class className, Object... pairs) {
        if (pairs == null || pairs.length <= 0) {
            return 0;
        }
        StringBuilder hql = new StringBuilder("DELETE FROM " + getSQLTableName(className) + " WHERE 1 = 1 ");
        Map<String, Object> mapParams = new HashMap<>();
        if (pairs != null) {
            int index = 0;
            String tempFieldName = "";
            for (Object obj : pairs) {
                if (index % 2 == 0) {
                    String fieldName = convertObjectNameToSqlName((String) obj);
                    tempFieldName = "fieldName" + index;
                    hql.append(" AND ").append(fieldName).append(" = :").append(tempFieldName);
                } else {
                    mapParams.put(tempFieldName, obj);
                }
                index++;
            }
        }
        return getJdbcTemplate().update(hql.toString(), mapParams);
    }

    public int deleteById(Long id, Class className, String idColumn) {
        if (id == null || id.equals(0L)) {
            return 0;
        }
        String hql = "DELETE FROM " + getSQLTableName(className)
                     + " WHERE " + convertObjectNameToSqlName(idColumn) + " = :id ";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return getJdbcTemplate().update(hql, params);
    }

    public int deleteByListId(List<Long> listId, Class className, String idColumn) {
        if (listId == null || listId.isEmpty()) {
            return 0;
        }
        String hql = "DELETE FROM " + getSQLTableName(className)
                     + " WHERE " + convertObjectNameToSqlName(idColumn) + " IN(:ids) ";
        Map<String, Object> params = new HashMap<>();
        List<List<Long>> listPartition = Utils.partition(listId, 999);
        int result = 0;
        for (List<Long> ids : listPartition) {
            params.put("ids", ids);
            result += getJdbcTemplate().update(hql, params);
        }
        return result;
    }

    public int deleteById(Class className, Long id) {
        if (id == null || id.equals(0L)) {
            return 0;
        }
        String hql = "DELETE FROM " + getSQLTableName(className)
                     + " WHERE " + getIdColumnName(className) + " = :id ";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return getJdbcTemplate().update(hql, params);
    }

    public int deleteByListId(Class className, List<Long> listId) {
        if (listId == null || listId.isEmpty()) {
            return 0;
        }
        String hql = "DELETE FROM " + getSQLTableName(className)
                     + " WHERE " + getIdColumnName(className) + " IN(:ids) ";
        Map<String, Object> params = new HashMap<>();
        List<List<Long>> listPartition = Utils.partition(listId, 999);
        int result = 0;
        for (List<Long> ids : listPartition) {
            params.put("ids", ids);
            result += getJdbcTemplate().update(hql, params);
        }
        return result;
    }

    public int deActiveObject(Class className, Long id) {
        if (id == null || id.equals(0L)) {
            return 0;
        }
        String hql = "UPDATE " + getSQLTableName(className) + " "
                     + " SET is_deleted = 'Y',"
                     + " modified_by = :userName,"
                     + " modified_time = :currentDate "
                     + " WHERE " + getIdColumnName(className) + " = :id ";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", Utils.getUserNameLogin());
        params.put("currentDate", new Date());
        params.put("id", id);
        return getJdbcTemplate().update(hql, params);
    }

    public int deActiveObjectByListId(Class className, List<Long> listId) {
        if (listId == null || listId.isEmpty()) {
            return 0;
        }
        String hql = "UPDATE " + getSQLTableName(className) + " "
                     + " SET is_deleted = 'Y',"
                     + " modified_by = :userName,"
                     + " modified_time = :currentDate "
                     + " WHERE " + getIdColumnName(className) + " IN(:ids) ";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", Utils.getUserNameLogin());
        params.put("currentDate", new Date());
        List<List<Long>> listPartition = Utils.partition(listId, 999);
        int result = 0;
        for (List<Long> ids : listPartition) {
            params.put("ids", ids);
            result += getJdbcTemplate().update(hql, params);
        }
        return result;
    }

    public int deActiveObject(Class className, String columnName, Object columnValue) {
        if (Utils.isNullObject(columnValue) || Utils.isNullOrEmpty(columnName)) {
            return 0;
        }
        String hql = "UPDATE " + getSQLTableName(className) + " "
                     + " SET is_deleted = 'Y',"
                     + " modified_by = :userName,"
                     + " modified_time = :currentDate "
                     + " WHERE " + convertObjectNameToSqlName(columnName) + " = :columnValue ";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", Utils.getUserNameLogin());
        params.put("currentDate", new Date());
        params.put("columnValue", columnValue);
        return getJdbcTemplate().update(hql, params);
    }

    public int deActiveObjectByListId(Class className, String columnName, List listValue) {
        if (listValue == null || listValue.isEmpty()) {
            return 0;
        }
        String hql = "UPDATE " + getSQLTableName(className) + " "
                     + " SET is_deleted = 'Y',"
                     + " modified_by = :userName,"
                     + " modified_time = :currentDate "
                     + " WHERE " + convertObjectNameToSqlName(columnName) + " IN(:pValues) ";
        Map<String, Object> params = new HashMap<>();
        params.put("userName", Utils.getUserNameLogin());
        params.put("currentDate", new Date());
        List<List<Object>> listPartition = Utils.partition(listValue, 999);
        int result = 0;
        for (List<Object> ids : listPartition) {
            params.put("pValues", ids);
            result += getJdbcTemplate().update(hql, params);
        }
        return result;
    }

    public int executeSqlDatabase(String queryString, Map<String, Object> mapParams) {
        return getJdbcTemplate().update(queryString, mapParams);
    }

    public int[] executeSqlDatabase(String[] queryString, Map<String, Object> hmapParams) {
        int[] result = new int[queryString.length];
        int count = 0;
        for (String sql : queryString) {
            result[count++] = getJdbcTemplate().update(sql, hmapParams);
        }
        return result;
    }

    public <T> T getFirstData(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            if (queryString.trim().endsWith("limit 1")) {
                if (className.isAssignableFrom(Date.class)) {
                    List<Date> dates = jdbcTemplate.query(
                            queryString,
                            mapParam,
                            (rs, rowNum) -> rs.getTimestamp(1)
                    );
                    return dates.isEmpty() ? null : (T) dates.get(0);
                } else {
                    return queryForObject(queryString, mapParam, className);
                }
            } else {
                String sql = queryString + " limit 1";
                if (className.isAssignableFrom(Date.class)) {
                    List<Date> dates = jdbcTemplate.query(
                            sql,
                            mapParam,
                            (rs, rowNum) -> rs.getTimestamp(1)
                    );
                    return dates.isEmpty() ? null : (T) dates.get(0);
                } else {
                    return queryForObject(sql, mapParam, className);
                }
            }

        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> T queryForObject(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            if (className.isAssignableFrom(Long.class)
                || className.isAssignableFrom(Integer.class)
                || className.isAssignableFrom(Double.class)
                || className.isAssignableFrom(String.class)
                || className.isAssignableFrom(Date.class)
            ) {
                return getJdbcTemplate().queryForObject(queryString, mapParam, className);
            } else {
                return (T) getJdbcTemplate().queryForObject(queryString, mapParam, new BeanPropertyRowMapper(className));
            }
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public <T> List<T> getListData(String queryString, Map<String, Object> mapParam, Class<T> className) {
        try {
            if (className.isAssignableFrom(Long.class)
                || className.isAssignableFrom(Integer.class)
                || className.isAssignableFrom(Double.class)
                || className.isAssignableFrom(String.class)
                || className.isAssignableFrom(Date.class)
            ) {
                return getJdbcTemplate().queryForList(queryString, mapParam, className);
            } else {
                return getJdbcTemplate().query(queryString, mapParam, new BeanPropertyRowMapper(className));
            }
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public <T> List<T> getListData(String queryString, Map<String, Object> mapParam, Class<T> className, String param, List listParams) throws ExecutionException, InterruptedException {
        List result = new ArrayList();
        if (listParams == null || listParams.isEmpty()) {
            return new ArrayList<>();
        } else {
            List<List> partitions = Utils.partition(listParams, 999);
            if (partitions.size() == 1) {
                mapParam.put(param, listParams);
                return getListData(queryString, mapParam, className);
            }

            List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
            partitions.stream().forEach(partition -> {
                Map mapParams = new HashMap(mapParam);
                mapParams.put(param, partition);
                Supplier<Object> datas = () -> getListData(queryString, mapParams, className);
                completableFutures.add(CompletableFuture.supplyAsync(datas));
            });
            CompletableFuture<Void> allReturn = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            CompletableFuture<List<Object>> allFutures = allReturn.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            List<Object> objs = allFutures.get();

            objs.stream().forEach(item -> {
                result.addAll((Collection) item);
            });
            return result;
        }

    }


    public List<Map<String, Object>> getListData(String queryString, Map<String, Object> mapParam) {
        try {
            return getJdbcTemplate().queryForList(queryString, mapParam);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public Long getSeqValue(String seqName) {
        String sql = "SELECT " + seqName + ".nextval FROM dual";
        return queryForObject(sql, new HashMap<>(), Long.class);
    }

    public <T> BaseDataTableDto<T> getListPagination(String queryString, Map<String, Object> mapParams, BaseSearchRequest request, Class<T> classOfT) {
        Integer startRecord = request.getStartRecord();
        Integer pageSize = request.getPageSize();
        try {
            pageSize = pageSize == null || pageSize < 0 ? BaseConstants.COMMON.DEFAULT_PAGE_SIZE : pageSize;

            String sqlCount = "SELECT COUNT(*) FROM (" + queryString + ") T";
            Long records = getJdbcTemplate().queryForObject(sqlCount, mapParams, Long.class);

            BaseDataTableDto dataTable = new BaseDataTableDto();

            if (records > 0) {
                String sqlPage = queryString +
                                 MessageFormat.format("  LIMIT {0} OFFSET {1}", String.valueOf(pageSize), String.valueOf(startRecord));
                if (classOfT.equals(TreeMap.class)) {
                    List mapQuery = getJdbcTemplate().queryForList(sqlPage, mapParams);
                    dataTable.setListData(mapQuery);
                } else {
                    List resultQuery = getJdbcTemplate().query(sqlPage, mapParams, new BeanPropertyRowMapper(classOfT));
                    dataTable.setListData(resultQuery);
                }
            } else {
                dataTable.setListData(new ArrayList());
            }
            dataTable.setTotal(records);
            dataTable.setPageIndex(startRecord / pageSize + 1);
            dataTable.setPageSize(pageSize);
            return dataTable;
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public int insertBatch(Class className, List listObject) {
        return this.insertBatch(className, listObject, null);
    }

    /**
     * insert ban ghi theo lo
     *
     * @param className
     * @param listObject
     * @return
     */
    public int insertBatch(Class className, List listObject, String createdBy) {
        try {
            if (listObject == null || listObject.isEmpty()) {
                return 0;
            }
            Field[] fields = className.getDeclaredFields();
            String tableName = getSQLTableName(className);

            StringBuilder insertTemplate = new StringBuilder("INSERT INTO " + tableName + "(");
            StringBuilder valueTemplate = new StringBuilder(" VALUES(");

            initInsertSQL(fields, insertTemplate, valueTemplate);
            // Lấy lớp cha của lớp con
            Class<?> parentClass = className.getSuperclass();
            if (parentClass != null) {
                initInsertSQL(parentClass.getDeclaredFields(), insertTemplate, valueTemplate);
            }

            String sqlInsert = insertTemplate.toString();
            sqlInsert = sqlInsert.substring(0, sqlInsert.length() - 1) + ")";
            String values = valueTemplate.toString();
            values = values.substring(0, values.length() - 1) + ")";

            Date currentDate = new Date();
            createdBy = createdBy == null ? Utils.getUserNameLogin() : createdBy;
            List<List<Object>> listPartition = Utils.partition(listObject, 5000);
            int successRecord = 0;
            for (List<Object> list : listPartition) {
                Map<String, Object>[] batchOfInputs = new HashMap[list.size()];
                int count = 0;
                for (Object object : list) {
                    Map<String, Object> mapParam = new HashMap<>();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String columnName = getColumnName(field);
                        if (!Utils.isNullOrEmpty(columnName)) {
                            Object value = field.get(object);
                            mapParam.put(field.getName(), value);
                        }
                    }
                    if (parentClass != null) {
                        for (Field field : parentClass.getDeclaredFields()) {
                            field.setAccessible(true);
                            String columnName = getColumnName(field);
                            if (StringUtils.equalsIgnoreCase(columnName, "created_by")) {
                                mapParam.put(field.getName(), createdBy);
                            } else if (StringUtils.equalsIgnoreCase(columnName, "created_time")) {
                                mapParam.put(field.getName(), currentDate);
                            }  else if (StringUtils.equalsIgnoreCase(columnName, "is_deleted")) {
                                mapParam.put(field.getName(), BaseConstants.STATUS.NOT_DELETED);
                            } else {
                                Object value = field.get(object);
                                mapParam.put(field.getName(), value);
                            }
                        }
                    }
                    batchOfInputs[count++] = mapParam;
                }
                long startTime = System.currentTimeMillis();
                int[] result = getJdbcTemplate().batchUpdate(sqlInsert + values, batchOfInputs);
                long duration = System.currentTimeMillis() - startTime;
                log.info(String.format("insertBatch | took %dms | sql-query: %s | size: %d", duration, sqlInsert + values, batchOfInputs.length));
                for (int i = 0; i < result.length; i++) {
                    int record = result[i];
                    if (record == 1) {
                        successRecord++;
                    } else {
                        log.info("INSERT FAIL|sqlInsert=" + sqlInsert + values + "|value=" + Utils.toJson(batchOfInputs[i]));
                    }
                }
            }
            return successRecord;
        } catch (IllegalAccessException ex) {
            log.error("ERROR=" + ex.getMessage(), ex);
        }
        return 0;
    }

    private void initInsertSQL(Field[] fields, StringBuilder insertTemplate, StringBuilder valueTemplate) {
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = getColumnName(field);
            if (!Utils.isNullOrEmpty(columnName)) {
                insertTemplate.append(columnName).append(",");
                valueTemplate.append(":").append(field.getName()).append(",");
            }
        }
    }

    /**
     * update ban ghi theo lo
     * Nhuoc diem: cau truc cac cot data phai giong nhau neu isUpdateNull = false
     *
     * @param className
     * @param listObject
     * @param isUpdateNull
     * @return
     */
    public int updateBatch(Class className, List listObject, boolean isUpdateNull) {
        try {
            if (listObject == null || listObject.isEmpty()) {
                return 0;
            }
            Field[] fields = className.getDeclaredFields();
            Class<?> parentClass = className.getSuperclass();
            if (parentClass != null) {
                fields = ArrayUtils.addAll(fields, parentClass.getDeclaredFields());
            }
            String tableName = getSQLTableName(className);

            String idColumnName = getIdColumnName(className);
            StringBuilder updateTemplate = new StringBuilder("UPDATE " + tableName + " SET ");
            // nhuoc diem: cau truc data phai giong nhau
            Object objectTemplate = listObject.get(0);
            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                Object value = field.get(objectTemplate);
                if (!Utils.isNullOrEmpty(columnName)
                    && !columnName.equalsIgnoreCase(idColumnName)
                    && (value != null || isUpdateNull)) {
                    updateTemplate.append(columnName).append(" = :").append(field.getName()).append(",");
                }
            }
            String sqlUpdate = updateTemplate.toString();
            sqlUpdate = sqlUpdate.substring(0, sqlUpdate.length() - 1);
            sqlUpdate += " WHERE " + idColumnName + " = :id";

            List<List<Object>> listPartition = Utils.partition(listObject, 999);
            int successRecord = 0;
            for (List<Object> list : listPartition) {
                Map<String, Object>[] batchOfInputs = new HashMap[list.size()];
                int count = 0;
                for (Object object : list) {
                    Map<String, Object> mapParam = new HashMap<>();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        String columnName = getColumnName(field);
                        if (!Utils.isNullOrEmpty(columnName)) {
                            Object value = field.get(object);
                            if (value != null || isUpdateNull) {
                                if (columnName.equalsIgnoreCase(idColumnName)) {
                                    mapParam.put("id", value);
                                } else {
                                    mapParam.put(field.getName(), value);
                                }
                            }
                        }
                    }
                    batchOfInputs[count++] = mapParam;
                }
                long startTime = System.currentTimeMillis();
                int[] result = getJdbcTemplate().batchUpdate(sqlUpdate, batchOfInputs);
                long duration = System.currentTimeMillis() - startTime;
                log.info(String.format("updateBatch | took %dms | sql-query: %s | size: %d", duration, sqlUpdate, batchOfInputs.length));
                for (int i = 0; i < result.length; i++) {
                    int record = result[i];
                    if (record == 1) {
                        successRecord++;
                    } else {
                        log.info("INSERT FAIL|sqlInsert=" + sqlUpdate + "|value=" + Utils.toJson(batchOfInputs[i]));
                    }
                }
            }
            return successRecord;
        } catch (IllegalAccessException ex) {
            log.error("ERROR=" + ex.getMessage(), ex);
        }
        return 0;
    }

    public <K, V> Map<K, V> getMapData(String sql, Map<String, Object> mapParam, String keyMapper, String valueMapper) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, mapParam);

        Map<K, V> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            if (row.containsKey(keyMapper) && row.containsKey(valueMapper)) {
                K key;
                Object rawKey = row.get(keyMapper);
                if (rawKey instanceof String) {
                    key = (K) rawKey.toString().toLowerCase();  // Chuyển key thành chữ thường nếu là String
                } else {
                    key = convertValue(rawKey);
                }
                Object rawValue = row.get(valueMapper);
                V value = convertValue(rawValue);
                result.put(key, value);
            }
        }
        return result;
    }

    public <K, V> Map<K, V> getMapData(String keyMapper, String valueMapper, Class className, Object... pairs) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE IFNULL(is_deleted, :activeStatus) = :activeStatus");
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        fillWhereColumns(sql, mapParam, pairs);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), mapParam);
        keyMapper = convertObjectNameToSqlName(keyMapper);
        valueMapper = convertObjectNameToSqlName(valueMapper);
        Map<K, V> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            if (row.containsKey(keyMapper) && row.containsKey(valueMapper)) {
                K key;
                Object rawKey = row.get(keyMapper);
                if (rawKey instanceof String) {
                    key = (K) rawKey.toString().toLowerCase();  // Chuyển key thành chữ thường nếu là String
                } else {
                    key = convertValue(rawKey);
                }
                Object rawValue = row.get(valueMapper);
                V value = convertValue(rawValue);
                result.put(key, value);
            }
        }
        return result;
    }

    public <K, V> Map<K, V> getMapData(String keyMapper, Class<V> className, Object... pairs) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + getSQLTableName(className) + " WHERE NVL(is_deleted, :activeStatus) = :activeStatus");
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        fillWhereColumns(sql, mapParam, pairs);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), mapParam);
        keyMapper = convertObjectNameToSqlName(keyMapper);
        Map<K, V> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            K key;
            Object rawKey = row.get(keyMapper);
            if (rawKey instanceof String) {
                key = (K) rawKey.toString().toLowerCase();  // Chuyển key thành chữ thường nếu là String
            } else {
                key = convertValue(rawKey);
            }
            V value = mapToObject(row, className); // Chuyển đổi Map sang đối tượng kiểu V
            result.put(key, value);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private <V> V convertValue(Object rawValue) {
        if (rawValue == null) {
            return null; // Trả về null nếu giá trị không tồn tại
        }
        if (rawValue instanceof BigDecimal) {
            // Chuyển đổi BigDecimal sang Long, Integer, Double, Float
            BigDecimal bigDecimal = (BigDecimal) rawValue;
            if (Long.class.isAssignableFrom((Class<V>) Long.class)) {
                return (V) Long.valueOf(bigDecimal.longValue());
            } else if (Integer.class.isAssignableFrom((Class<V>) Integer.class)) {
                return (V) Integer.valueOf(bigDecimal.intValue());
            } else if (Double.class.isAssignableFrom((Class<V>) Double.class)) {
                return (V) Double.valueOf(bigDecimal.doubleValue());
            } else if (Float.class.isAssignableFrom((Class<V>) Float.class)) {
                return (V) Float.valueOf(bigDecimal.floatValue());
            } else {
                throw new IllegalArgumentException("Unsupported BigDecimal conversion");
            }
        } else if (rawValue instanceof String) {
            // Nếu rawValue là String, cast trực tiếp
            return (V) rawValue;
        } else {
            // Các trường hợp khác: cast trực tiếp
            return (V) rawValue;
        }
    }


    public <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            Class<?> parentClass = clazz.getSuperclass();
            if (parentClass != null) {
                fields = ArrayUtils.addAll(fields, parentClass.getDeclaredFields());
            }
            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = convertObjectNameToSqlName(field.getName()).toUpperCase();
                Object value = map.get(fieldName);

                if (value != null) {
                    Class<?> fieldType = field.getType();

                    // Xử lý kiểu BigDecimal -> Long hoặc Integer
                    if (value instanceof BigDecimal) {
                        if (fieldType == Long.class || fieldType == long.class) {
                            value = ((BigDecimal) value).longValue();
                        } else if (fieldType == Integer.class || fieldType == int.class) {
                            value = ((BigDecimal) value).intValue();
                        } else if (fieldType == Double.class || fieldType == double.class) {
                            value = ((BigDecimal) value).doubleValue();
                        } else if (fieldType == Float.class || fieldType == float.class) {
                            value = ((BigDecimal) value).floatValue();
                        }
                    }

                    // Gán giá trị cho trường
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping data to object", e);
        }
    }

    public void executeBatch(String sql, List<Map> listParams) {
        long startTime = System.currentTimeMillis();
        getJdbcTemplate().batchUpdate(sql, listParams.toArray(new HashMap[listParams.size()]));
        long duration = System.currentTimeMillis() - startTime;
        log.info(String.format("executeBatch | took %dms | sql-query: %s | size: %d", duration, sql, listParams.size()));
    }

    private String getIdColumnName(Class className) {
        for (Field f : className.getDeclaredFields()) {
            Id id = f.getAnnotation(Id.class);
            if (id != null) {
                Column column = f.getAnnotation(Column.class);
                return column.name();
            }
        }
        return null;
    }

    private String getSequenceName(Class className) {
        for (Field f : className.getDeclaredFields()) {
            SequenceGenerator seq = f.getAnnotation(SequenceGenerator.class);
            if (seq != null) {
                return seq.sequenceName();
            }
        }
        return null;
    }

    public Long getIdColumnValue(Object object) throws IllegalAccessException {
        for (Field field : object.getClass().getDeclaredFields()) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    return Long.valueOf(value.toString());
                }
            }
        }
        return null;
    }

    private String getColumnName(Field f) {
        Column column = f.getAnnotation(Column.class);
        if (column != null) {
            return column.name();
        } else {
            return null;
        }
    }

    public String convertObjectNameToSqlName(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append("_").append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString().toLowerCase();
    }

    public String getSQLTableName(Class className) {
        Table table = (Table) className.getAnnotation(Table.class);
        return table.name();
    }

    public Long getNextId(Class className) {
        return getNextId(className, 1);
    }

    public Long getNextId(Class className, int increment) {
        String tableName = ((Table) className.getAnnotation(Table.class)).name();
        String sql = "SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = :tableName";
        HashMap<String, Object> params = new HashMap();

        String schemaName = getCurrentSchema();
        if (StringUtils.isNotBlank(schemaName)) {
            sql += " and table_schema = :schemaName";
            params.put("schemaName", schemaName);
        }

        params.put("tableName", tableName);
        Long autoIncrement = queryForObject(sql, params, Long.class);

        String sqlAlter = "ALTER TABLE " + tableName + " AUTO_INCREMENT = :valueInc";
        params.put("valueInc", autoIncrement + increment);
        executeSqlDatabase(sqlAlter, params);
        return autoIncrement;
    }

    public String getCurrentSchema() {
        return getJdbcTemplate().queryForObject("SELECT DATABASE() FROM DUAL", new HashMap<>(), String.class);
    }

    public Map<String, Object> getMapEmptyAliasColumns(String sqlQuery) {
        List<String> aliasColumns = getReturnAliasColumns(sqlQuery);
        Map<String, Object> map = new HashMap<>();
        map.put("stt", "");
        aliasColumns.stream().forEach(item -> {
            map.put(item, null);
        });
        return map;
    }

    public List<String> getReturnAliasColumns(String sqlQuery) {
        List<String> aliasColumns = new ArrayList();
        sqlQuery = sqlQuery.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        sqlQuery = sqlQuery.replace("\t", " ");
        int numOfRightPythis = 0;
        int startPythis = -1;
        int endPythis = 0;
        boolean hasRightPythis = true;
        while (hasRightPythis) {
            char[] arrStr = sqlQuery.toCharArray();
            hasRightPythis = false;
            int idx = 0;
            for (char c : arrStr) {
                if (idx > startPythis) {
                    if ("(".equalsIgnoreCase(String.valueOf(c))) {
                        if (numOfRightPythis == 0) {
                            startPythis = idx;
                        }
                        numOfRightPythis++;
                    } else if (")".equalsIgnoreCase(String.valueOf(c))) {
                        if (numOfRightPythis > 0) {
                            numOfRightPythis--;
                            if (numOfRightPythis == 0) {
                                endPythis = idx;
                                break;
                            }
                        }
                    }
                }
                idx++;
            }
            if (endPythis > 0) {
                sqlQuery = sqlQuery.substring(0, startPythis) + " # " + sqlQuery.substring(endPythis + 1);
                hasRightPythis = true;
                endPythis = 0;
            }
        }
        int index = sqlQuery.toUpperCase().indexOf(" FROM ");
        if (index == -1) {
            index = sqlQuery.toUpperCase().indexOf("\nFROM ");

            if (index == -1) {
                index = sqlQuery.toUpperCase().indexOf("\tFROM ");
            }
        }
        String arrStr[] = sqlQuery.substring(0, index).split(",");
        for (String str : arrStr) {
            String[] temp = str.trim().split(" ");
            String alias = temp[temp.length - 1].trim();
            if (alias.contains(".")) {
                alias = alias.substring(alias.lastIndexOf(".") + 1).trim();
            }
            if (alias.contains(",")) {
                alias = alias.substring(alias.lastIndexOf(",") + 1).trim();
            }
            if (!aliasColumns.contains(alias)) {
                aliasColumns.add(alias);
            }
        }
        return aliasColumns;
    }

    public List<Map<String, Object>> getSysdate() {
        return getListData("select now() as currentTime from dual", new HashMap<>());
    }

    public <T> T getCategory(String categoryType, String value, Class<T> className) throws IllegalAccessException {
        String sql = "select sc.* from sys_categories sc where sc.category_type = :categoryType " +
                     " and sc.value = :value" +
                     " and sc.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("value", value);
        mapParams.put("categoryType", categoryType);

        T categoryDto = (T) queryForObject(sql, mapParams, className);

        List<String> codes = extractAttributeCodes(className);
        if (!codes.isEmpty()) {
            setCategoryAttribute(className, mapParams, categoryDto);
        }

        return categoryDto;
    }

    public <T> List<T> getListCategory(String categoryType, Class<T> className) throws IllegalAccessException {
        String sql = "select sc.* from sys_categories sc where sc.category_type = :categoryType " +
                     " and sc.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("categoryType", categoryType);

        List<T> categoryDtos = getListData(sql, mapParams, className);

        List<String> codes = extractAttributeCodes(className);
        if (!codes.isEmpty()) {
            categoryDtos.forEach(item -> {
                try {
                    setCategoryAttribute(className, mapParams, item);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return categoryDtos;
    }

    public List<Map<String, Object>> getMapCategory(String categoryType) throws IllegalAccessException {
        String sql = "select sc.* from sys_categories sc where sc.category_type = :categoryType " +
                     " and sc.is_deleted = 'N'";
        Map mapParams = new HashMap();
        mapParams.put("categoryType", categoryType);

        List<Map<String, Object>> resultList = getListData(sql, mapParams);
        if (Utils.isNullOrEmpty(resultList)) {
            resultList.add(getMapEmptyAliasColumns(sql));
        }
        return resultList;
    }

    private static <T> List<String> extractAttributeCodes(Class<T> className) {
        List<String> codes = new ArrayList<>();
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(Attribute.class) != null) {
                codes.add(field.getAnnotation(Attribute.class).code().toUpperCase());
            }
        }
        return codes;
    }

    private <T> void setCategoryAttribute(Class<T> className, Map mapParams, T categoryDto) throws IllegalAccessException {
        String sqlAttributes = "select attribute_code, attribute_value " +
                               " from sys_category_attributes st" +
                               " where st.is_deleted = 'N'" +
                               " and st.category_id = :categoryId";
        mapParams.put("categoryId", ((BaseCategoryDto) categoryDto).getCategoryId());
        List<Map<String, Object>> entities = getListData(sqlAttributes, mapParams);
        Map<String, String> mapValues = new HashMap<>();
        entities.stream().forEach(item -> {
            mapValues.put(((String) item.get("ATTRIBUTE_CODE")).toUpperCase(), (String) item.get("ATTRIBUTE_VALUE"));
        });
        // Iterate through the fields and set their values from mapValues
        for (Field field : className.getDeclaredFields()) {
            field.setAccessible(true);
            Attribute parameter = field.getAnnotation(Attribute.class);
            String attributeCode = parameter == null ? null : parameter.code().toUpperCase();
            if (attributeCode != null) {
                String attributeValue = mapValues.get(attributeCode);
                if (attributeValue == null) {
                    attributeValue = "";
                }
                if (attributeValue != null) {
                    Class<?> fieldType = field.getType();
                    if ("java.lang.String".equalsIgnoreCase(fieldType.getName())) {
                        field.set(categoryDto, attributeValue);
                    } else if ("java.lang.Double".equalsIgnoreCase(fieldType.getName())) {
                        field.set(categoryDto, Double.valueOf(attributeValue));
                    } else if ("java.lang.Long".equalsIgnoreCase(fieldType.getName())) {
                        field.set(categoryDto, Long.valueOf(attributeValue));
                    } else if ("java.lang.Integer".equalsIgnoreCase(fieldType.getName())) {
                        field.set(categoryDto, Integer.valueOf(attributeValue));
                    } else if ("java.util.Date".equalsIgnoreCase(fieldType.getName())) {
                        field.set(categoryDto, Utils.stringToDate(attributeValue));
                    } else if (fieldType.isAssignableFrom(List.class) && field.getGenericType() instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        if (genericType.equals(Long.class)) {
                            List<Long> longList = Arrays.stream(attributeValue.split(","))
                                    .map(Long::valueOf)
                                    .collect(Collectors.toList());
                            field.set(categoryDto, longList);
                        } else if (genericType.equals(String.class)) {
                            attributeValue = attributeValue.replace(" ", "");
                            field.set(categoryDto, Arrays.asList(attributeValue.split(",")));
                        }
                    }
                } else {
                    throw new BaseAppException("Chưa có dữ dữ liệu cấu hình của " + field.getAnnotation(Attribute.class).code());
                }
            }
        }
    }

    public int deActiveObjectByPairList(Class className, Pair<String, List>... pairConditionList) {
        if (pairConditionList == null || pairConditionList.length == 0) {
            return 0;
        }
        StringBuilder hql = new StringBuilder("UPDATE " + getSQLTableName(className) + " "
                                              + " SET is_deleted = 'Y',"
                                              + " modified_by = :userName,"
                                              + " modified_time = :currentDate "
                                              + " WHERE is_deleted = 'N' ");
        Map<String, Object> params = new HashMap<>();
        for (Pair<String, List> item : pairConditionList) {
            if (!Utils.isNullOrEmpty(item.getRight())) {
                hql.append(" AND " + convertObjectNameToSqlName(item.getLeft()) + " IN (:" + item.getLeft() + ")");
                params.put(item.getLeft(), item.getRight());
            }
        }
        params.put("userName", Utils.getUserNameLogin());
        params.put("currentDate", new Date());
        int result = 0;
        result += jdbcTemplate.update(hql.toString(), params);
        return result;
    }
	
	private void fillWhereColumns(StringBuilder sql, Map<String, Object> mapParam, Object... pairs) {
        if (pairs != null) {
            int index = 0;
            String tempFieldName = "";
            String orderByField = "";
            for (Object obj : pairs) {
                if (index % 2 == 0) {
                    String fieldName = convertObjectNameToSqlName((String) obj);
                    if (pairs.length - 1 == index) {//cot chan
                        orderByField = fieldName;
                    } else {
                        tempFieldName = "fieldName" + index;
                        sql.append(" AND ");
                        Object nextValue = pairs[index + 1];

                        if (nextValue instanceof Collection) {
                            sql.append(fieldName).append(" IN (:").append(tempFieldName).append(")");
                        } else {
                            sql.append(fieldName).append(" = :").append(tempFieldName);
                        }
                    }
                } else {
                    mapParam.put(tempFieldName, obj);
                }
                index++;
            }
            if (!Utils.isNullOrEmpty(orderByField)) {
                sql.append(" ORDER BY ").append(orderByField);
            }
        }
    }
}

