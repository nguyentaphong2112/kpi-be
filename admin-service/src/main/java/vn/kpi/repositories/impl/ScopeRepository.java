package vn.kpi.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.kpi.models.response.ScopeResponse;
import vn.kpi.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ScopeRepository extends BaseRepository {
    public List<ScopeResponse> getScopes() {
        String sql = "select * from sys_scopes a " +
                " where a.is_deleted = 'N'" +
                " order by a.order_number";
        return getListData(sql, new HashMap<>(), ScopeResponse.class);
    }

    public Long getScopeId(String code) {
        String sql = "select scope_id from sys_scopes a " +
                " where a.is_deleted = 'N'" +
                "   and a.code = :code";
        Map map = new HashMap<>();
        map.put("code", code);
        return queryForObject(sql, map, Long.class);
    }
}
