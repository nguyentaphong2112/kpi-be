package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.nfunk.jep.function.Str;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
@RequiredArgsConstructor
public class CommonRepository extends BaseRepository {
    public Map<String, String> getMapFullAddress(List<String> listProvinceName) {
        ConcurrentMap<String, String> mapResult =  new ConcurrentHashMap<>();
        if (Utils.isNullOrEmpty(listProvinceName)) {
            return mapResult;
        }
        String sql = """
                    SELECT
                        concat(sc.name,'#',sc1.name,'#',sc2.name) name,
                        concat(sc.value,'#',sc1.value,'#',sc2.value) value
                    FROM sys_categories sc
                    JOIN sys_categories sc1 ON sc1.category_type = :districtCode
                    JOIN sys_categories sc2 ON sc2.category_type = :wardCode
                    WHERE sc.category_type = :provinceCode
                    AND sc.is_deleted = 'N'
                    AND sc1.is_deleted = 'N'
                    AND sc2.is_deleted = 'N'
                    AND lower(sc.name) IN (:listProvinceName)
                    AND EXISTS (
                        SELECT 1 FROM sys_category_attributes ct
                        WHERE ct.category_id = sc1.category_id
                        AND ct.attribute_code = 'MA_TINH'
                        AND ct.is_deleted = 'N'
                        AND ct.attribute_value = sc.value
                    )
                    AND EXISTS (
                        SELECT 1 FROM sys_category_attributes ct
                        WHERE ct.category_id = sc2.category_id
                        AND ct.attribute_code = 'MA_HUYEN'
                        AND ct.is_deleted = 'N'
                        AND ct.attribute_value = sc1.value
                    )
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("wardCode", Constant.CATEGORY_TYPES.XA);
        params.put("districtCode", Constant.CATEGORY_TYPES.HUYEN);
        params.put("provinceCode", Constant.CATEGORY_TYPES.TINH);
        params.put("listProvinceName", listProvinceName);

        List<CategoryEntity> listData = getListData(sql, params, CategoryEntity.class);
        listData.parallelStream().forEach(entity -> mapResult.put(entity.getName().toLowerCase(), entity.getValue()));
        return mapResult;
    }

    public Map<String, String> getMapNameValueByNameList(String categoryType, List<String> nameList) {
        StringBuilder sql = new StringBuilder("""
                SELECT name, value
                FROM sys_categories sc
                WHERE ifnull(sc.is_deleted, :isDeleted) = :isDeleted
                    AND sc.category_type = :categoryType
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("categoryType", categoryType);

        QueryUtils.filter(nameList, sql, params, "sc.name");
        Map<String, String> mapNameAndValue = new HashMap<>();
        List<CategoryEntity> categoryList = getListData(sql.toString(), params, CategoryEntity.class);

        categoryList.forEach(item -> mapNameAndValue.put(StringUtils.lowerCase(item.getName()), StringUtils.trim(item.getValue())));

        return mapNameAndValue;
    }
}
