/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.models.response.InsuranceSalaryProcessResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;


/**
 * Lop repository Impl ung voi bang hr_insurance_salary_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class InsuranceSalaryProcessRepository extends BaseRepository {

    public List<InsuranceSalaryProcessResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                a.insurance_factor,
                a.insurance_base_salary,
                a.reserve_factor,
                a.seniority_percent,
                a.created_by,
                a.created_time,
                a.modified_by,
                a.modified_time,
                a.last_update_time,
                a.start_date,
                a.end_date
            FROM hr_insurance_salary_process a
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
            ORDER BY a.start_date DESC
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empId", empId);
        return getListData(sql, params, InsuranceSalaryProcessResponse.class);
    }

}
