/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.TemplateParamsDTO;
import vn.hbtplus.models.response.TemplateParamsResponse;
import vn.hbtplus.repositories.BaseRepository;

import java.util.HashMap;

/**
 * Lop repository Impl ung voi bang PNS_TEMPLATE_PARAMS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public class TemplateParamsRepositoryImpl extends BaseRepository {

    public BaseDataTableDto<TemplateParamsResponse> searchData(TemplateParamsDTO dto) {
        String sql = """
                    SELECT
                        a.template_param_id,
                        a.code,
                        a.name,
                        a.created_by,
                        a.create_date,
                        a.last_updated_by,
                        a.last_update_date,
                        a.is_deleted,
                        a.default_value
                    FROM pns_template_params a
                    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus
                """;
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, params, dto, TemplateParamsResponse.class);
    }
}
