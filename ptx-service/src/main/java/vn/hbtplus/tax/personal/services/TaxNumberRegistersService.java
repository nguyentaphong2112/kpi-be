/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.security.SignatureException;
import java.util.List;

import vn.hbtplus.tax.personal.models.dto.EmployeeInfosDTO;
import vn.hbtplus.tax.personal.models.dto.PersonalIdentitiesDTO;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.RejectDTO;
import vn.hbtplus.tax.personal.models.request.TaxNumberRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.models.response.EmployeeInfoResponse;
import vn.hbtplus.tax.personal.models.response.TaxNumberRegistersResponse;
import vn.hbtplus.tax.personal.repositories.entity.TaxNumberRegistersEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Lop interface service ung voi bang PTX_TAX_NUMBER_REGISTERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TaxNumberRegistersService {

    TableResponseEntity searchData(TaxNumberRegistersDTO dto) throws SignatureException;

    ResponseEntity<Object> saveData(TaxNumberRegistersDTO dto, List<MultipartFile> files, boolean isAdmin);

    ResponseEntity<Object> deleteData(Long id, boolean isAdmin);

    BaseResponseEntity<TaxNumberRegistersResponse> getDataById(Long id, boolean isAdmin) throws SignatureException;

    ResponseEntity<Object> updateWorkFLow( Long id, Integer status, boolean isAdmin);

    ResponseEntity<Object> exportNewRegister(AdminSearchDTO dto) throws Exception;

    ResponseEntity<Object> exportChangeRegister(AdminSearchDTO dto) throws Exception;

    ResponseEntity<Object> approveByList(List<Long> listId);

    ResponseEntity<Object> approveAll(AdminSearchDTO dto);

    ResponseEntity<Object> rejectByList(RejectDTO dto);

    ListResponseEntity<TaxNumberRegistersEntity> getRecentRegister(Long employeeId, boolean isAdmin);

    ResponseEntity<Object> getImportTemplateNewRegisterResult() throws Exception;

    ResponseEntity<Object> importNewRegisterResult(MultipartFile file, HttpServletRequest req);

    ResponseEntity<Object> getImportTemplateChangeRegisterResult() throws Exception;

    ResponseEntity<Object> importChangeRegisterResult(MultipartFile file, HttpServletRequest req);

    ResponseEntity<Object> exportRegisterByTaxOfficeTemplate(TaxNumberRegistersDTO dto) throws Exception;

    EmployeeInfosDTO getContactInfo(Long employeeId);

    List<PersonalIdentitiesDTO> getPersonalIdentities(Long id);
}
