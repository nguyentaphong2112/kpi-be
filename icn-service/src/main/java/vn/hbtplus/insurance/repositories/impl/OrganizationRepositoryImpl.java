package vn.hbtplus.insurance.repositories.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.models.OrganizationDto;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrganizationRepositoryImpl extends BaseRepository {
    public Map<String, Long> createOrgMap(List<String> orgNameLevelList) {
        Map<String, Long> orgMap = new HashMap<>();
        if (Utils.isNullOrEmpty(orgNameLevelList)) {
            return orgMap;
        }
        String sql = """
                SELECT CONCAT(IFNULL(org_name_level1, ''), IFNULL(org_name_level2, ''),IFNULL(org_name_level3, ''),IFNULL(org_name_level4, ''),IFNULL(org_name_level5, '')) org_name,
                    org_id
                FROM hr_organizations
                WHERE ifnull(is_deleted, :isDeleted) = :isDeleted
                    AND CONCAT(IFNULL(org_name_level1, ''), IFNULL(org_name_level2, ''),IFNULL(org_name_level3, ''),IFNULL(org_name_level4, ''),IFNULL(org_name_level5, ''))
                        IN (:orgNameLevels)
                order by org_level, org_id
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("orgNameLevels", orgNameLevelList);
        params.put("isDeleted", BaseConstants.STATUS.NOT_DELETED);

        List<OrganizationDto> orgList = getListData(sql, params, OrganizationDto.class);
        orgList.forEach(item -> {
            String orgName = StringUtils.lowerCase(item.getOrgName());
            if (orgMap.get(orgName) == null) {
                orgMap.put(orgName, item.getOrgId());
            }
        });

        return orgMap;
    }
}
