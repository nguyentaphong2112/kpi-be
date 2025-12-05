/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.PositionsResponse;
import vn.hbtplus.repositories.BaseRepository;
import vn.hbtplus.models.request.ContractTypesRequest;
import vn.hbtplus.models.response.ContractTypesResponse;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.QueryUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lop repository Impl ung voi bang hr_contract_types
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Repository
@RequiredArgsConstructor
public class ContractTypesRepository extends BaseRepository {

    public BaseDataTableDto searchData(ContractTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.contract_type_id,
                    a.code,
                    a.name,
                    a.emp_type_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time,
                    a.order_number,
                    c.name classifyCode,
                    b.name emp_type_name
                    FROM hr_contract_types a
                    LEFT JOIN hr_emp_types b ON (a.emp_type_id = b.emp_type_id AND IFNULL(b.is_deleted, :activeStatus) = :activeStatus)
                    LEFT JOIN sys_categories c ON (c.category_type = :categoryType and c.value = a.classify_code)
                """);
        HashMap<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        params.put("categoryType", BaseConstants.CATEGORY_CODES.PHAN_LOAI_HOP_DONG);
        return getListPagination(sql.toString(), params, dto, ContractTypesResponse.SearchResult.class);
    }

    public List<Map<String, Object>> getListExport(ContractTypesRequest.SearchForm dto) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.contract_type_id,
                    a.code,
                    a.name,
                    a.emp_type_id,
                    a.is_deleted,
                    a.created_by,
                    a.created_time,
                    a.modified_by,
                    a.modified_time,
                    a.last_update_time
                    FROM hr_contract_types a
                """);
        Map<String, Object> params = new HashMap<>();
        addCondition(sql, params, dto);
        return getListData(sql.toString(), params);
    }

    private void addCondition(StringBuilder sql, Map<String, Object> params, ContractTypesRequest.SearchForm dto) {
        sql.append("""
                    WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
                """);
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        if (!Utils.isNullOrEmpty(dto.getKeySearch())) {
            sql.append(" AND (lower(a.code) like :keySearch or lower(a.name) like :keySearch)");
            params.put("keySearch", "%" + dto.getKeySearch() + "%");
        }
        if (dto.getEmpTypeId() != null && dto.getEmpTypeId() > 0) {
            sql.append(" AND (lower(a.emp_type_id) = :empTypeId)");
            params.put("empTypeId", dto.getEmpTypeId());
        }
        //QueryUtils.filterGe(dto.getFromDate(), sql, params, "a.from_date", "fromDate");
        //QueryUtils.filterLe(dto.getToDate(), sql, params, "a.from_date", "toDate");
//        sql.append(" ORDER BY mo.display_seq, mo.path_id");
    }

    public List<ContractTypesResponse.DetailBean> getListContractType(String classifyCode, Long empTypeId) {
        StringBuilder sql = new StringBuilder("""
                    SELECT a.contract_type_id,
                        a.code,
                        a.name,
                        a.emp_type_id
                    FROM hr_contract_types a
                    WHERE a.is_deleted = :activeStatus
                """);
        Map<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        QueryUtils.filter(classifyCode, sql, params, "a.classify_code");
        QueryUtils.filter(empTypeId, sql, params, "a.emp_type_id");
        return getListData(sql.toString(), params, ContractTypesResponse.DetailBean.class);
    }


    public Map<String, List<ContractTypesResponse.DetailBean>> getMapContractType(String classifyCode, Long empTypeId) {
        List<ContractTypesResponse.DetailBean> listData = getListContractType(classifyCode, empTypeId);
        Map<String, List<ContractTypesResponse.DetailBean>> result = new HashMap<>();
        for (ContractTypesResponse.DetailBean detailBean : listData) {
            result.computeIfAbsent(detailBean.getName().toLowerCase(), k -> new ArrayList<>());
            result.get(detailBean.getName().toLowerCase()).add(detailBean);
        }
        return result;
    }
}
