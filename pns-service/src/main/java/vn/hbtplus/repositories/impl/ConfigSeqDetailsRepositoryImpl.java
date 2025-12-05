/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.repositories.impl;

import org.springframework.stereotype.Repository;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.ConfigSeqDetailsDTO;
import vn.hbtplus.models.response.ConfigSeqDetailsResponse;
import vn.hbtplus.repositories.BaseRepository;


import java.util.HashMap;

/**
 * Lop repository Impl ung voi bang PNS_CONFIG_SEQ_DETAILS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Repository
public class ConfigSeqDetailsRepositoryImpl extends BaseRepository {

    public BaseDataTableDto<ConfigSeqDetailsResponse> searchData(ConfigSeqDetailsDTO dto) {
        String sql = "SELECT "
                + "    a.config_seq_detail_id,"
                + "    a.config_seq_contract_id,"
                + "    a.contract_type_id,"
                + "    a.order_number,"
                + "    a.is_deleted,"
                + "    a.created_by,"
                + "    a.create_date,"
                + "    a.last_updated_by,"
                + "    a.last_update_date"
                + "    FROM pns_config_seq_details a"
                + "    WHERE IFNULL(a.is_deleted, :flagStatus) = :flagStatus";
        HashMap<String, Object> params = new HashMap<>();
        params.put("flagStatus", BaseConstants.STATUS.NOT_DELETED);
        return getListPagination(sql, params, dto, ConfigSeqDetailsResponse.class);
    }
}
