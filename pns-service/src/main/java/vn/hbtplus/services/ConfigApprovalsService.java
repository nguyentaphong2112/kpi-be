/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.dto.ConfigApprovalsDTO;
import vn.hbtplus.models.dto.ContractApproversDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ConfigApprovalsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.security.SignatureException;
import java.util.Date;

/**
 * Lop interface service ung voi bang PNS_CONFIG_APPROVALS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ConfigApprovalsService {

    TableResponseEntity<ConfigApprovalsResponse> searchData(ConfigApprovalsDTO dto);

    BaseResponseEntity<Object> saveData(ConfigApprovalsDTO dto, MultipartFile file) throws SignatureException;

    BaseResponseEntity<Object> deleteData(Long id);

    BaseResponseEntity<Object> getDataById(Long id) throws SignatureException;

    ContractApproversDTO getApprover(Integer type, Long empId, Long posId, Long orgId, Date reportDate);

    ResponseEntity<Object> exportData(ConfigApprovalsDTO dto) throws Exception;
}
