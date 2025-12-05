package vn.hbtplus.insurance.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.insurance.repositories.entity.PositionGroupMappingEntity;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PositionGroupMappingRepositoryImpl extends BaseRepository {
    public List<Map<String, Object>> getAllCategoryByType(String type) {
        String sql = """
                select name cat_name
                from sys_categories
                where category_type = :type and ifnull(is_deleted, :isDeleted) = :isDeleted
                order by name
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);
        params.put("type", type);
        List result = getListData(sql, params);
        if (result.isEmpty()) {
            result.add(getMapEmptyAliasColumns(sql));
        }

        return result;
    }

    public Map<String, PositionGroupMappingEntity> createMapPositionGroup(List<Long> orgIds) {
        Map<String, PositionGroupMappingEntity> positionGroupMap = new HashMap<>();
        if (Utils.isNullOrEmpty(orgIds)) {
            return positionGroupMap;
        }
        String sql = """
                select * from hr_position_group_mappings 
                where org_id in (:orgIds)
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("orgIds", orgIds);
        List<PositionGroupMappingEntity> entityList = getListData(sql, params, PositionGroupMappingEntity.class);
        String formatKey = "jobId%s#orgId%s#groupType%s";
        entityList.forEach(item -> {
            String key = String.format(formatKey, item.getJobId(), item.getOrgId(), item.getGroupType());
            positionGroupMap.put(StringUtils.lowerCase(key), item);
        });

        return positionGroupMap;
    }
}
