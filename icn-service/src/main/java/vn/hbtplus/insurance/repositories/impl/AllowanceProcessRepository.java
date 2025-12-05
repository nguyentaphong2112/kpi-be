/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.response.AllowanceProcessResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;


/**
 * Lop repository Impl ung voi bang hr_allowance_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class AllowanceProcessRepository extends BaseRepository {

    public List<AllowanceProcessResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                (select s.name from sys_categories s where s.value = a.allowance_type and s.category_type = :categoryType) allowanceTypeName,
                a.factor,
                a.amount,
                a.start_date,
                a.end_date,
                a.created_by,
                a.created_time,
                a.modified_by,
                a.modified_time,
                a.last_update_time
            FROM hr_allowance_process a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
            ORDER BY a.start_date DESC
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empId", empId);
        params.put("categoryType", Constant.CATEGORY_TYPE.LOAI_PHU_CAP);
        return getListData(sql, params, AllowanceProcessResponse.class);
    }

}
