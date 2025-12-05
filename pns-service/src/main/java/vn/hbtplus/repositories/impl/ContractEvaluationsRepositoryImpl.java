/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.response.ContractEvaluationsResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;

/**
 * Lop repository Impl ung voi bang PNS_CONTRACT_EVALUATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public class ContractEvaluationsRepositoryImpl extends BaseRepository {

    public BaseDataTableDto<ContractEvaluationsResponse> searchData(ContractEvaluationsDTO dto) {
        String sql = """
                    SELECT
                        a.contract_evaluation_id,
                        a.contract_proposal_id,
                        a.employee_id,
                        a.is_disciplined,
                        a.disciplined_note,
                        a.kpi_point,
                        a.rank_code,
                        a.rank_name,
                        a.is_deleted,
                        a.created_by,
                        a.create_date,
                        a.last_updated_by,
                        a.last_update_date
                    FROM pns_contract_evaluations a
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, params, dto, ContractEvaluationsResponse.class);
    }
}
