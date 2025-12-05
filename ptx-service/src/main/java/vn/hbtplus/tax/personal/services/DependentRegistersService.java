/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.util.List;

import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.models.dto.FamilyRelationshipsDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DependentRegistersDTO;
import vn.hbtplus.tax.personal.models.request.RejectDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DependentRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.HrFamilyRelationshipsEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Lop interface service ung voi bang PTX_DEPENDENT_REGISTERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DependentRegistersService {

    TableResponseEntity<DependentRegistersResponse> searchData(AdminSearchDTO dto) throws SignatureException;

    ResponseEntity<Object> exportDataDependentRegister(AdminSearchDTO dto);

    ResponseEntity<Object> saveData(DependentRegistersDTO dto, List<MultipartFile> files, boolean isAdmin);

    ResponseEntity<Object> deleteData(Long id, boolean isAdmin);

    BaseResponseEntity<DependentRegistersResponse> getDataById(Long id, boolean isAdmin) throws SignatureException;

    ResponseEntity<Object> updateWorkFLow(Long id, Integer status, boolean isAdmin);

    ResponseEntity<Object> approveByList(List<Long> listId);

    ResponseEntity<Object> approveAll(AdminSearchDTO dto);

    ResponseEntity<Object> rejectByList(RejectDTO dto);

    ResponseEntity<Object> getImportTemplateRegisterResult() throws Exception;

    ResponseEntity<Object> importRegisterResult(MultipartFile file, HttpServletRequest req);

    ResponseEntity<Object> exportDataAccordingTaxAuthority(AdminSearchDTO dto);

    BaseResponseEntity<Object> autoRegister(Long employeeId, String cancelDate, String clientMessageId);

    ResponseEntity<Object> sendMailConfirmDependent(AdminSearchDTO dto);

    ResponseEntity<Object> getListDataByEmpId(FamilyRelationshipsDTO familyRelationshipsDTO);
}
