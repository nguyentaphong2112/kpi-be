
package vn.hbtplus.utils;

import vn.hbtplus.models.PermissionDataDto;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tudd
 */
public class QueryUtils {

    public static void filter(String str, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if ((str != null) && !"".equals(str.trim())) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND LOWER(").append(field).append(") LIKE :").append(key).append(" ESCAPE '/'");
            str = str.trim().replaceAll(" +", " ");
            str = "%" + str.trim().toLowerCase().replace("/", "//").replace("_", "/_").replace("%", "/%") + "%";
            mapParam.put(key, str);
        }
    }

    /**
     * tim kiem dieu kien or theo nhieu cot
     *
     * @param str         : xau
     * @param queryString
     * @param mapParam
     * @param fields
     */
    public static void filter(String str, StringBuilder queryString, Map<String, Object> mapParam, String... fields) {
        if (!Utils.isNullOrEmpty(str)) {
            queryString.append(" AND ( 0 = 1 ");
            str = str.trim().replaceAll(" +", " ");
            str = "%" + str.trim().toLowerCase().replace("/", "//").replace("_", "/_").replace("%", "/%") + "%";
            int i = 0;
            for (String field : fields) {
                String key = convertSQLNameToJavaName(field);
                queryString.append(" OR LOWER(").append(field).append(") LIKE :").append(key).append(i).append(" ESCAPE '/'");
                mapParam.put(key + i, str);
                i++;
            }
            queryString.append(")");
        }
    }

    /**
     * tim kiem co phan biet hoa, thuong
     *
     * @param str
     * @param queryString
     * @param mapParam
     * @param fields
     */
    public static void filterOriginal(String str, StringBuilder queryString, Map<String, Object> mapParam, String... fields) {
        if (!Utils.isNullOrEmpty(str)) {
            queryString.append(" AND ( 0 = 1 ");
            str = str.trim().replaceAll(" +", " ");
            str = "%" + str.trim().replace("/", "//").replace("_", "/_").replace("%", "/%") + "%";
            int i = 0;
            for (String field : fields) {
                String key = convertSQLNameToJavaName(field);
                queryString.append(" OR ").append(field).append(" LIKE :").append(key).append(i).append(" ESCAPE '/'");
                mapParam.put(key + i, str);
                i++;
            }
            queryString.append(")");
        }
    }

    public static void filterEq(String str, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullOrEmpty(str)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND LOWER(").append(field).append(") = :").append(key);
            str = str.trim().replaceAll(" +", " ");
            str = str.trim().toLowerCase();
            mapParam.put(key, str);
        }
    }

    public static void filterNotEq(String str, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullOrEmpty(str)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND LOWER(").append(field).append(") != :").append(key);
            str = str.trim().replaceAll(" +", " ");
            str = str.trim().toLowerCase();
            mapParam.put(key, str);
        }
    }

    public static void filterNotEq(Long id, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (id != null) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" != :").append(key);
            mapParam.put(key, id);
        }
    }

    public static void filter(Long n, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullObject(n)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" = :").append(key);
            mapParam.put(key, n);
        }
    }

    public static void filter(Long[] n, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        filter(Arrays.asList(n), queryString, mapParam, field);
    }

    public static void filter(List<?> n, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullOrEmpty(n)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" IN (:").append(key).append(")");
            mapParam.put(key, n);
        }
    }

    public static void filter(Integer n, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if ((n != null) && (n >= 0)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" = :").append(key);
            mapParam.put(key, n);
        }
    }

    /**
     * kiem tra 1 xau rong hay null khong
     *
     * @param n           So
     * @param queryString
     * @param mapParam
     * @param field
     */
    public static void filter(Double n, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if ((n != null) && (n >= 0)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" = :").append(key);
            mapParam.put(key, n);
        }
    }

    public static void filter(Date date, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if ((date != null)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" = :").append(key);
            mapParam.put(key, date);
        }
    }

    /**
     * Kiem tra lon hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     */
    public static void filterGe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field) {
        if (obj != null && !"".equals(obj)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" >= :").append(key);
            paramMap.put(key, obj);
        }
    }

    /**
     * Kiem tra nho hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     */
    public static void filterLe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field) {
        if (obj != null && !"".equals(obj)) {
            String key = convertSQLNameToJavaName(field);
            queryString.append(" AND ").append(field).append(" <= :").append(key);
            paramMap.put(key, obj);
        }
    }

    /**
     * Kiem tra lon hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     * @param keyMap
     */
    public static void filterLe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field, String keyMap, String conditionOr) {
        if (obj != null && !"".equals(obj)) {
            queryString.append(" AND ( ").append(field).append(" <= :").append(keyMap).append(" OR ").append(field).append(" ").append(conditionOr).append(" ) ");
            paramMap.put(keyMap, obj);
        }
    }

    /**
     * Kiem tra lon hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     * @param keyMap
     */
    public static void filterGe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field, String keyMap) {
        if (obj != null && !"".equals(obj)) {
            queryString.append(" AND ").append(field).append(" >= :").append(keyMap);
            paramMap.put(keyMap, obj);
        }
    }

    /**
     * Kiem tra lon hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     * @param keyMap
     */
    public static void filterGe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field, String keyMap, String conditionOr) {
        if (obj != null && !"".equals(obj)) {
            queryString.append(" AND ( ").append(field).append(" >= :").append(keyMap).append(" OR ").append(field).append(" ").append(conditionOr).append(" ) ");
            paramMap.put(keyMap, obj);
        }
    }

    /**
     * Kiem tra nho hon hoac bang.
     *
     * @param obj         So
     * @param queryString
     * @param paramMap
     * @param field
     * @param keyMap
     */
    public static void filterLe(Object obj, StringBuilder queryString, Map<String, Object> paramMap, String field, String keyMap) {
        if (obj != null && !"".equals(obj)) {
            queryString.append(" AND ").append(field).append(" <= :").append(keyMap);
            paramMap.put(keyMap, obj);
        }
    }

    public static void filterConflictDate(Object objFrom, Object objTo, StringBuilder queryString, Map<String, Object> paramMap,
                                          String fieldFrom, String fieldTo, String keyMapFrom, String keyMapTo) {
        if (objFrom != null && !"".equals(objFrom) && objTo != null && !"".equals(objTo)) {
            queryString.append(" AND ( :").append(keyMapFrom).append(" <= ").append(fieldTo).append(" OR ").append(fieldTo).append(" IS NULL ) ")
                    .append(" AND :").append(keyMapTo).append(" >= ").append(fieldFrom);
            paramMap.put(keyMapFrom, objFrom);
            paramMap.put(keyMapTo, objTo);
        } else if (objFrom != null && !"".equals(objFrom)) {
            queryString.append(" AND :").append(keyMapFrom).append(" >= ").append(fieldFrom).append(" AND ( :").append(keyMapFrom).append(" <= ").append(fieldTo)
                    .append(" OR ").append(fieldTo).append(" IS NULL ) ");
            paramMap.put(keyMapFrom, objFrom);
        } else if (objTo != null && !"".equals(objTo)) {
            queryString.append(" AND :").append(keyMapTo).append(" >= ").append(fieldFrom).append(" AND ( :").append(keyMapTo).append(" <= ").append(fieldTo)
                    .append(" OR ").append(fieldTo).append(" IS NULL ) ");
            paramMap.put(keyMapTo, objTo);
        }
    }

    public static void filterOrg(Long orgId, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullObject(orgId)) {
            queryString.append(" AND ").append(field).append(" LIKE :orgId");
            mapParam.put("orgId", "%/" + orgId + "/%");
        }
    }

    public static void filterLikeOrg(List<Long> orgIds, StringBuilder queryString, Map<String, Object> mapParam, String field) {
        if (!Utils.isNullOrEmpty(orgIds)) {
            int index = 0;
            queryString.append(" AND (0=1 ");
            for (Long orgId : orgIds) {
                String key = "pathId" + (index++);
                queryString.append(" OR ").append(field).append(" LIKE :").append(key);
                mapParam.put(key, "%/" + orgId + "/%");
            }
            queryString.append(" ) ");
        }
    }

    public static String convertSQLNameToJavaName(String str) {

        str = str.trim().replaceAll(" ", "");
        StringBuilder result = new StringBuilder();
        boolean isUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                isUpperCase = true;
                continue;
            }
            if (isUpperCase) {
                result.append(Character.toUpperCase(c));
                isUpperCase = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static void addConditionPermission(List<PermissionDataDto> permissionData, StringBuilder sql, Map<String, Object> params, String... columnFilters) {
        sql.append(" and (0 = 1");
        if (!Utils.isNullOrEmpty(permissionData)) {
            for (int i = 0; i < permissionData.size(); i++) {
                PermissionDataDto permissionDataDto = permissionData.get(i);
                sql.append(" OR (1=1");

                if (columnFilters.length == 0 || columnFilters[0] != null) {
                    if (!Utils.isNullOrEmpty(permissionDataDto.getOrgIds())) {
                        String pathIdColumn = columnFilters.length > 0 ? columnFilters[0] : "o.path_id";
                        sql.append(MessageFormat.format("""
                                    and exists (
                                        select 1 from hr_organizations pOrg
                                        where pOrg.organization_id in (:orgPermissionId{0})
                                        and {1} like concat(pOrg.path_id,''%'')
                                    )
                                """, i, pathIdColumn));
                        params.put(MessageFormat.format("orgPermissionId{0}", i), permissionDataDto.getOrgIds());
                    } else {
                        sql.append(" and 1=0");
                    }
                }

                if (columnFilters.length == 0 || (columnFilters.length > 1 && columnFilters[1] != null)) {
                    if (!Utils.isNullOrEmpty(permissionDataDto.getEmpTypeIds())) {
                        String empTypeColumn = columnFilters.length > 1 ? columnFilters[1] : "e.emp_type_id";
                        sql.append(MessageFormat.format(" and {0} in (:empTypePermissionId{1})", empTypeColumn, i));
                        params.put(MessageFormat.format("empTypePermissionId{0}", i), permissionDataDto.getEmpTypeIds());
                    }
                }

                sql.append(")");
            }
        }
        sql.append(")");
    }

    public static void filterExpression(String productPriceFilter, StringBuilder sql, Map<String, Object> params, String columnFilter) {
        if (!Utils.isNullOrEmpty(productPriceFilter)) {
            try {
                String regex = "([><=]+)(\\d+)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(productPriceFilter.trim().replace(" ", ""));
                String operator = "=";  // Mặc định toán tử là "="
                String number = productPriceFilter.trim();
                if (matcher.matches()) {
                    operator = matcher.group(1);
                    number = matcher.group(2);
                }
                params.put(convertSQLNameToJavaName(columnFilter), Double.valueOf(number.replaceAll("[^0-9]", "")));
                sql.append(String.format(" AND %s %s :%s", columnFilter, operator,
                        convertSQLNameToJavaName(columnFilter)));
            } catch (Exception ex) {
            }
        }
    }


}
