/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ApproveDTO;
import vn.hbtplus.models.dto.ContractFeesDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.ContractFeesResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.ContractFeesEntity;

import javax.servlet.http.HttpServletRequest;
import java.security.SignatureException;
import java.util.List;

/**
 * Lop interface service ung voi bang PNS_CONTRACT_FEES
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractFeesService {

    TableResponseEntity<ContractFeesResponse> searchData(ContractFeesDTO dto) throws SignatureException;

    ResponseEntity<Object> saveData(ContractFeesDTO dto);
    ResponseEntity<Object> saveListData(List<ContractFeesEntity> dto);

    ResponseEntity<Object> deleteData(Long id);

    ResponseEntity<Object> getDataById(Long id) throws SignatureException;

    ResponseEntity<Object> exportData(ContractFeesDTO dto) throws Exception;

    ResponseEntity<Object> approveById(ApproveDTO dto);

    ResponseEntity<Object> approveByList(ApproveDTO dto);

    ResponseEntity<Object> approveAll(ContractFeesDTO dto);

    ResponseEntity<Object> rejectById(RejectDTO dto);

    ResponseEntity<Object> rejectByList(RejectDTO dto);

    ResponseEntity<Object> importContractFee(MultipartFile file, HttpServletRequest req) throws Exception;

    ResponseEntity<Object> getTemplateImport() throws Exception;
}
