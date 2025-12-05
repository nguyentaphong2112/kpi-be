/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import com.aspose.words.Document;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.dto.ContractTemplatesDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractTemplatesResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractProposalsEntity;
import vn.hbtplus.repositories.entity.HrContractTypesEntity;

import java.io.IOException;

/**
 * Lop interface service ung voi bang PNS_CONTRACT_TEMPLATES
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractTemplatesService {

    TableResponseEntity<ContractTemplatesResponse> searchData(ContractTemplatesDTO dto);

    BaseResponseEntity<Object> saveData(ContractTemplatesDTO dto, String userNameLogin) throws IOException;

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id);

    ListResponseEntity<HrContractTypesEntity> getContractTypes();

    byte[] downloadContractTemplates(Long id);

    Document getTemplateContract(ContractProposalsEntity entity);

    ResponseEntity<Object> exportData(ContractTemplatesDTO dto) throws Exception;
}
