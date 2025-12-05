package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.DomainRequest;
import vn.kpi.models.response.DomainResponse;
import vn.kpi.repositories.BaseRepository;
import vn.kpi.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DomainRepository extends BaseRepository {

    public List<DomainResponse.DomainDto> getDomains(String type) {
        String sql = """
                select a.domain_id, a.code, a.name 
                from sys_domains a
                where a.domain_type = :domainType
                order by a.order_number 
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("domainType", type);
        return getListData(sql, map, DomainResponse.DomainDto.class);
    }

    public List<DomainResponse.DomainDto> getRootNodes(String type) {
        String sql = """
                select a.domain_id, a.code, a.name 
                from sys_domains a
                where a.domain_type = :domainType
                and a.parent_id is null
                order by a.path_order, a.order_number 
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("domainType", type);
        return getListData(sql, map, DomainResponse.DomainDto.class);
    }

    public List<DomainResponse.DomainDto> getChildrenNodes(String type, String parentKey) {
        String sql = """
                select a.domain_id, a.code, a.name 
                from sys_domains a
                where a.domain_type = :domainType
                and a.parent_id = :parentKey
                order by a.path_order, a.order_number 
                """;
        Map<String, Object> map = new HashMap<>();
        map.put("domainType", type);
        map.put("parentKey", parentKey);
        return getListData(sql, map, DomainResponse.DomainDto.class);
    }

    public BaseDataTableDto<DomainResponse.DomainDto> search(String type, DomainRequest.SearchForm request) {
        StringBuilder sql = new StringBuilder("""
                select a.domain_id, a.code, a.name
                from sys_domains a
                where a.domain_type = :domainType
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("domainType", type);
        if (!Utils.isNullOrEmpty(request.getParentKey())) {
            sql.append(" and a.path_id like :parentKey");
            params.put("parentKey", "%/" + request.getParentKey() + "/%");
        }
        if (!Utils.isNullOrEmpty(request.getKeySearch())) {
            sql.append(" and (a.code like upper(:keySearch) or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + request.getKeySearch() + "%");
        }
        sql.append(" order by a.path_order, a.order_number");
        return getListPagination(sql.toString(), params, request, DomainResponse.DomainDto.class);
    }

    public List<DomainResponse.DefaultDto> getCategories(String categoryType) {
        String sql = """
                 select value, name
                 from sys_categories 
                 where is_deleted = 'N'
                   and category_type = :categoryType
                   order by order_number, name
                   """;
        Map<String, Object> map = new HashMap<>();
        map.put("categoryType", categoryType);
        return getListData(sql, map, DomainResponse.DefaultDto.class);
    }
}
