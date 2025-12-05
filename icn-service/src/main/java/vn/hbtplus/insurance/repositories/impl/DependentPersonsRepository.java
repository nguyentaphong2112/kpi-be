/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.insurance.repositories.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.insurance.models.response.DependentPersonsResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;
import java.util.List;


/**
 * Lop repository Impl ung voi bang hr_dependent_persons
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
@RequiredArgsConstructor
@Slf4j
public class DependentPersonsRepository extends BaseRepository {

    public List<DependentPersonsResponse> getDataByEmpId(Long empId) {
        String sql = """
            SELECT
                (SELECT sc.name FROM sys_categories sc WHERE sc.value = fr.relation_type_id AND sc.category_type = :relationTypeCode) relationTypeName,
                a.from_date startDate,
                a.to_date endDate,
                fr.full_name,
                a.tax_no taxNumber,
                a.created_by,
                a.create_date createdTime
            FROM hr_dependent_persons a
            JOIN hr_family_relationships fr ON fr.family_relationship_id = a.family_relationship_id
            WHERE IFNULL(a.is_deleted, :activeStatus) = :activeStatus
            AND a.employee_id = :empId
            ORDER BY a.from_date
        """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("activeStatus", BaseConstants.STATUS.NOT_DELETED);
        params.put("empId", empId);
        params.put("relationTypeCode", Constant.CATEGORY_TYPE.MOI_QUAN_HE);
        return getListData(sql, params, DependentPersonsResponse.class);
    }

}
