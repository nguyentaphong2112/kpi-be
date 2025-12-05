/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.dto.ConfigSeqContractsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigSeqContractsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang PNS_CONFIG_SEQ_CONTRACTS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ConfigSeqContractsService {

    TableResponseEntity<ConfigSeqContractsResponse> searchData(ConfigSeqContractsDTO dto);

    BaseResponseEntity<Object> saveData(ConfigSeqContractsDTO dto);

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id);

    ResponseEntity<Object> exportData(ConfigSeqContractsDTO dto) throws Exception;
}
