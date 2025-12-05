package vn.kpi.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.kpi.feigns.PermissionFeignClient;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.PermissionDataDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.dto.OrgLevelDTO;
import vn.kpi.models.request.OrganizationsRequest;
import vn.kpi.models.response.OrganizationsResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.repositories.entity.OrganizationsEntity;
import vn.kpi.services.AuthorizationService;
import vn.kpi.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class OrganizationTreeRepository extends BaseRepository {
    private final PermissionFeignClient permissionFeignClient;
    private final HttpServletRequest request;
    private final AuthorizationService authorizationService;

    public List<TreeDto> getRootNodes(String scope, String functionCode) {
        List<Long> listOrgId = getListOrgIdByPermission(scope, functionCode);
        StringBuilder sql = new StringBuilder("""
                    SELECT
                        rc.organization_id nodeId,
                        rc.name,
                        rc.code,
                        rc.parent_id parentId,
                        rc.path_id
                    FROM hr_organizations rc
                        WHERE rc.is_deleted = 'N'
                        
                """);
        Map<String, Object> params = new HashMap<>();

        if (listOrgId == null) {
            sql.append(" and rc.parent_id IS NULL");
        } else if (listOrgId.isEmpty()) {
            sql.append(" and 0 = 1");
        } else {
            sql.append(" and rc.organization_id in (:listOrgId)");
            params.put("listOrgId", listOrgId);
        }
        sql.append(" order by rc.path_order");
        return getListData(sql.toString(), params, TreeDto.class);
    }

    public List<TreeDto> getChildrenNodes(Long parentId) {
        String sql = """
                SELECT
                    rc.organization_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id,
                    (select count(*) from hr_organizations c 
                        where c.parent_id = rc.organization_id
                        and c.is_deleted = 'N'
                    ) as totalChildren
                FROM hr_organizations rc 
                    WHERE rc.is_deleted = 'N'
                    and rc.parent_id = :parentId
                    order by rc.path_order
                """;
        Map params = new HashMap();
        params.put("parentId", parentId);
        return getListData(sql, params, TreeDto.class);
    }

    public List<TreeDto> getAllOrganizations() {
        String sql = """
                SELECT
                    rc.organization_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM hr_organizations rc 
                    WHERE rc.is_deleted = 'N'
                    order by rc.path_order
                """;
        return getListData(sql, new HashMap<>(), TreeDto.class);
    }

    public BaseDataTableDto search(OrganizationsRequest.SearchForm request) {
        List<Long> listOrgId = getListOrgIdByPermission(request.getScope(), request.getFunctionCode());
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.organization_id as nodeId,
                    a.parent_id,
                    (select name from hr_organizations p where p.organization_id = a.parent_id) as parentName,
                    a.name,
                    a.code
                from hr_organizations a
                where a.is_deleted = 'N'
                """);
        HashMap<String, Object> params = new HashMap<>();
        if (request.getOrganizationId() != null){
            sql.append(" and a.path_id like :orgPath");
            params.put("orgPath", "%/" + request.getOrganizationId() + "/%");
        }
        if(!Utils.isNullOrEmpty(request.getKeySearch())){
            sql.append(" and (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + request.getKeySearch().toLowerCase() + "%");
        }

        if (listOrgId != null && listOrgId.isEmpty()) {
            sql.append(" and 0 = 1");
        } else if (!Utils.isNullOrEmpty(listOrgId)) {
            sql.append("""
                        and exists (
                            select 1 from hr_organizations pOrg
                            where pOrg.organization_id in (:listOrgId)
                            and a.path_id like concat(pOrg.path_id, '%')
                        )
                    """);
            params.put("listOrgId", listOrgId);
        }
        sql.append(" order by a.path_level, a.path_order");
        return getListPagination(sql.toString(), params, request, OrganizationsResponse.SearchTreeChooseResult.class);
    }

    public List<TreeDto> getByListNodeId(List<String> listNodeId) {
        if (Utils.isNullOrEmpty(listNodeId)) {
            return new ArrayList<>();
        }
        String sql = """
                SELECT
                    rc.organization_id nodeId,
                    rc.name,
                    rc.code,
                    rc.parent_id parentId,
                    rc.path_id
                FROM hr_organizations rc
                    WHERE rc.is_deleted = 'N'
                    AND rc.organization_id in (:listNodeId)
                    order by rc.path_order
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("listNodeId", listNodeId);
        return getListData(sql, params, TreeDto.class);
    }

    private List<Long> getListOrgIdByPermission(String scope, String functionCode) {
        List<Long> listOrgId = null;
        if (!Utils.isNullOrEmpty(scope) && !Utils.isNullOrEmpty(functionCode)) {
            List<PermissionDataDto> permissionData = authorizationService.getPermissionData(scope, functionCode, Utils.getUserNameLogin());
            if (!Utils.isNullOrEmpty(permissionData)) {
                Set<Long> setOrgId = new HashSet<>();
                for (PermissionDataDto dto: permissionData) {
                    if (!Utils.isNullOrEmpty(dto.getOrgIds())) {
                        setOrgId.addAll(dto.getOrgIds());
                    }
                }
                listOrgId = new ArrayList<>(setOrgId);

                List<Long> listRemoveIds = new ArrayList<>();
                for (Long orgId : listOrgId) {
                    OrganizationsEntity organizationsEntity = get(OrganizationsEntity.class, orgId);
                    String path = organizationsEntity == null ? "" : organizationsEntity.getPathId();
                    for (Long id : listOrgId) {
                        if (path == null || (path.contains("/" + id + "/") && !id.equals(orgId))) {
                            listRemoveIds.add(orgId);
                            break;
                        }
                    }
                }
                listOrgId = listOrgId.stream().filter(item -> !listRemoveIds.contains(item)).toList();
            }
        }
        return listOrgId;
    }


    public List<OrgLevelDTO> getOrgByLevel(OrgLevelDTO dto) {
        HashMap<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("""
                 SELECT
                  mo.organization_id orgId,
                  mo.name orgName,
                  mo.path_id pathId,
                  mo.org_level_manage orgLevel
                  FROM hr_organizations mo
                  where IFNULL(mo.org_type_id, '') <> 'TMP'
                  and mo.is_deleted = 'N'
                """);

        if (dto.getOrgLevel() != null) {
            sql.append(" AND mo.org_level_manage = :orgLevel");
            params.put("orgLevel", dto.getOrgLevel());
        }
        if (dto.getOrgId() != null) {
            sql.append(" AND mo.parent_id = :parentId");
            params.put("parentId", dto.getOrgId());
        }
        if (dto.getInputDate() != null) {
            sql.append(" AND IFNULL(mo.start_date, :inputDate) <= :inputDate AND IFNULL(mo.end_date, :inputDate) >= :inputDate");
            params.put("inputDate", dto.getInputDate());
        }
        sql.append(" ORDER BY mo.path_id");
        return getListData(sql.toString(), params, OrgLevelDTO.class);
    }
}
