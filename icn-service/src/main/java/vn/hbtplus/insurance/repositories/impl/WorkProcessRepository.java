/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.models.response.WorkProcessResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;


/**
 * Lop repository Impl ung voi bang hr_work_process
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
public class WorkProcessRepository extends BaseRepository {

    public List<WorkProcessResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                a.start_date,
                a.end_date,
                (select j.name from hr_jobs j where j.job_id = a.job_id) jobName,
                o.full_name orgName,
                (select dt.name from hr_document_types dt where dt.document_type_id = a.document_type_id) documentTypeName,
                a.document_no,
                a.created_by,
                a.created_time,
                a.modified_by,
                a.modified_time,
                a.last_update_time
            FROM hr_work_process a
            JOIN hr_organizations o ON o.organization_id = a.organization_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empId", empId);
        return getListData(sql, params, WorkProcessResponse.class);
    }

}
