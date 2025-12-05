/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.response.ContractProcessResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;


/**
 * Lop repository Impl ung voi bang hr_contract_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class ContractProcessRepository extends BaseRepository {

    public List<ContractProcessResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                ets.name empTypeName,
                ct.name contractTypeName,
                a.start_date,
                a.end_date,
                ets.code emp_type_code,
                a.document_no,
                a.created_by,
                a.created_time,
                a.modified_by,
                a.modified_time,
                a.last_update_time
            FROM hr_contract_process a
            LEFT JOIN hr_contract_types ct ON ct.contract_type_id = a.contract_type_id
            LEFT JOIN hr_emp_types ets ON ets.emp_type_id = a.emp_type_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
            ORDER BY a.start_date DESC
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
//        params.put("categoryType", Constant.CATEGORY_TYPE.DOI_TUONG);

//        params.put("armyRankType", Constant.CATEGORY_TYPE.CAP_BAC_QUAN_HAM);
        params.put("empId", empId);
        return getListData(sql, params, ContractProcessResponse.class);
    }

}
