package vn.hbtplus.tax.income.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.tax.income.models.bean.CategoryAttributeBean;
import vn.hbtplus.tax.income.models.bean.CategoryBean;
import vn.hbtplus.tax.income.models.response.CategoryResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CategoryRepository extends BaseRepository {

    public List<CategoryResponse> getCategories(String categoryType) {
        String sql = "select category_id, value, name, code, order_number from sys_categories " +
                " where is_deleted = 'N'" +
                "   and category_type = :categoryType" +
                "   order by order_number, name";
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return getListData(sql, map, CategoryResponse.class);
    }

    public Map<String, CategoryResponse> createCategoryMap(String categoryType) {
        List<CategoryResponse> categoryResponseList = getCategories(categoryType);
        Map<String, CategoryResponse> mapCategory = new HashMap<>();

        if (Utils.isNullOrEmpty(categoryResponseList)) {
            return mapCategory;
        }

        categoryResponseList.forEach(item -> mapCategory.put(StringUtils.lowerCase(item.getName()), item));

        return mapCategory;
    }

    public List<CategoryBean> getListCategoryBeans(String categoryType) {
        String sql = "select category_id, value, name, code, order_number from sys_categories " +
                " where is_deleted = 'N'" +
                "   and category_type = :categoryType" +
                "   order by order_number, name";
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        List<CategoryBean> results = getListData(sql, map, CategoryBean.class);
        //Lay thong tin attributes
        sql = """
                select object_attribute_id, attribute_code, attribute_value
                 from hr_object_attributes attr
                   where attr.is_deleted = 'N'
                   and attr.object_attribute_id in (
                        select category_id from sys_categories
                        where is_deleted = 'N'
                        and category_type = :categoryType
                   )
               """;
        List<CategoryAttributeBean> lstAttributes = getListData(sql, map, CategoryAttributeBean.class);
        Map<Long, Map<String, String>> mapValues = new HashMap<>();
        lstAttributes.stream().forEach(item -> {
            if(mapValues.get(item.getCategoryId()) == null){
                mapValues.put(item.getCategoryId(), new HashMap<>());
            }
            mapValues.get(item.getCategoryId()).put(item.getAttributeCode().toLowerCase(), item.getValue());
        });
        results.forEach(bean -> {
            bean.setAttributes(mapValues.get(bean.getCategoryId()));
        });
        return results;
    }
}
