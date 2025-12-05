package vn.hbtplus.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WbMaterialRequest;
import vn.hbtplus.models.response.WbMaterialResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.utils.QueryUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class WbMaterialRepository extends BaseRepository {
	public BaseDataTableDto searchData(WbMaterialRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    eg.wb_material_id,
                    eg.code,
                    eg.name,
                    eg.barcode,
                    eg.seri,
                    eg.number,
                    eg.description,
                    eg.wb_material_type_id,
                    eg.company_id,
                    eg.country_id,
                    eg.unit_id,
                    eg.is_deleted,
                    eg.created_by,
                    eg.created_time,
                    eg.modified_by,
                    eg.modified_time,
                    eg.last_update_time,
                    e.name wbMaterialTypeName
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListPagination(sql.toString(), params, dto, WbMaterialResponse.class);
    }
	
    private void addCondition(StringBuilder sql, Map<String, Object> params, WbMaterialRequest.SearchForm dto) {
        sql.append("""
            FROM wb_material eg
            LEFT JOIN wb_material_type e ON e.wb_material_type_id = eg.wb_material_type_id
            WHERE IFNULL(eg.is_deleted, :activeStatus) = :activeStatus
        """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(dto.getWbMaterialTypeId(), sql, params, "eg.wb_material_type_id");
        QueryUtils.filter(dto.getName(), sql, params, "eg.name");
        QueryUtils.filter(dto.getCode(), sql, params, "eg.code");
        sql.append(" ORDER BY eg.created_time DESC");
    }
    

    public List<WbMaterialResponse> getAll() {
        String sql = """
                    select wb_material_id, code, name, barcode, seri, number
                    from wb_material eg
                    WHERE IFNULL(eg.is_deleted, :activeStatus) = :activeStatus
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);

        return getListData(sql, params, WbMaterialResponse.class);
    }
}
