/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.dto.ContractEvaluationsDTO;
import vn.hbtplus.models.dto.RejectDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ContractApproversResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.security.SignatureException;
import java.util.List;

/**
 * Lop interface service ung voi bang PNS_CONTRACT_APPROVERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ContractApproversService {

    TableResponseEntity<ContractApproversResponse> searchData(ContractApproversDTO dto);

    BaseResponseEntity<Object> saveData(ContractApproversDTO dto, List<MultipartFile> files);

    BaseResponseEntity<Object> getDataById(Long id) throws SignatureException;

    BaseResponseEntity<Object> keepSigningByList(@RequestParam List<Long> listId);

    BaseResponseEntity<Object> keepSigningAll(@RequestBody ContractApproversDTO dto);

    BaseResponseEntity<Object> liquidationByList(@RequestBody RejectDTO dto);

    BaseResponseEntity<Object> countValidRecord(ContractApproversDTO dto);

    BaseResponseEntity<Object> saveEvaluation(ContractEvaluationsDTO dto, List<MultipartFile> files);
}
