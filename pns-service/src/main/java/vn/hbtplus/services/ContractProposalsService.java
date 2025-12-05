/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ContractProposalsDTO;
import vn.hbtplus.models.dto.ContractProposalsFormDTO;
import vn.hbtplus.models.response.ContractProposalsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractProposalsEntity;
import vn.hbtplus.repositories.entity.HrContractTypesEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Lop interface service ung voi bang PNS_CONTRACT_PROPOSALS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractProposalsService {

    TableResponseEntity<ContractProposalsResponse> searchData(ContractProposalsDTO dto);

    ResponseEntity<Object> saveData(ContractProposalsFormDTO dto);

    ResponseEntity<Object> getDataById(Long id);

    ResponseEntity<Object> exportContract(Long id) throws Exception;

    void autoFindExpiredContract(Date fromDate, Date toDate);

    void autoFindNewContract();

    void autoFindAppendixContract();

    ResponseEntity<Object> exportContractByForm(ContractProposalsDTO dto) throws Exception;

    ResponseEntity<Object> exportContractByListId(List<Long> listId)  throws Exception;

    ResponseEntity<Object> exportData(ContractProposalsDTO dto);

    ResponseEntity<Object> uploadFileSigned(List<MultipartFile> fileExtends, Integer type) throws IOException;

    ResponseEntity<Object> updateStatusFileSigned(List<ContractProposalsDTO> listDTO);

    String getNewOrContinueContractNumber(HrContractTypesEntity contractTypesEntity, ContractProposalsEntity entity);

    ResponseEntity<Object> deleteDataById(Long id);

    ResponseEntity<Object> deleteByForm(ContractProposalsDTO dto);

    ResponseEntity<Object> sendMailByList(ContractProposalsDTO dto) throws Exception;

    ResponseEntity<Object> sendMailAll(ContractProposalsDTO dto) throws Exception;

    ResponseEntity<Object> importTemplateContractFee() throws Exception;

    ResponseEntity<Object> importProcessContractFee(MultipartFile file, HttpServletRequest req) throws IOException;

    String getFileNameContract(ContractProposalsEntity entity) throws Exception;
}
