/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractEvaluationsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang PNS_CONTRACT_EVALUATIONS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractEvaluationsService {

    TableResponseEntity<ContractEvaluationsResponse> searchData(ContractEvaluationsDTO dto);

    BaseResponseEntity<Object> saveData(ContractEvaluationsDTO dto, List<MultipartFile> files);

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id);

}
