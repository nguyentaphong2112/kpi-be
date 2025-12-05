package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WbMaterialTypeRequest;
import vn.hbtplus.models.response.WbMaterialTypeResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WbMaterialTypeRepository extends BaseRepository {
	public BaseDataTableDto searchData(WbMaterialTypeRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    eg.wb_material_type_id,
                    eg.code,
                    eg.name,
                    eg.symbol,
                    eg.description,
                    eg.is_deleted,
                    eg.created_by,
                    eg.created_time,
                    eg.modified_by,
                    eg.modified_time,
                    eg.last_update_time,
                    e.name parentName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WbMaterialTypeResponse.class);
    }
	
    private void addCondition(StringBuilder sql, Map<String, Object> params, WbMaterialTypeRequest.SearchForm dto) {
        sql.append("""
            FROM wb_material_type eg
            LEFT JOIN wb_material_type e ON e.wb_material_type_id = eg.parent_id
            WHERE IFNULL(eg.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getSymbol(), sql, params, "eg.symbol");
        QueryUtils.filter(dto.getName(), sql, params, "eg.name");
        QueryUtils.filter(dto.getCode(), sql, params, "eg.code");
        sql.append(" ORDER BY eg.created_time DESC");
    }
    

    public List<WbMaterialTypeResponse> getAll() {
        String sql = """
                    select wb_material_type_id, code, name, symbol, description
                    from wb_material_type eg
                    WHERE IFNULL(eg.is_deleted, :activeStatus) = :activeStatus
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        return getListData(sql, params, WbMaterialTypeResponse.class);
    }
}
