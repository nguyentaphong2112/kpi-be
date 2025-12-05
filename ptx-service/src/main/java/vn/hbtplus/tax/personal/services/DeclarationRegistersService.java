/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.DeclarationRegistersDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.DeclarationRegistersResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.personal.repositories.entity.DeclarationRegistersEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Lop interface service ung voi bang PTX_DECLARATION_REGISTERS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface DeclarationRegistersService {

    TableResponseEntity<DeclarationRegistersResponse> searchData(AdminSearchDTO dto);

    ResponseEntity<Object> exportData(AdminSearchDTO dto);

    ResponseEntity<Object> saveData(DeclarationRegistersDTO dto, boolean isAdmin);

    ResponseEntity<Object> deleteData(Long id, boolean isAdmin);

    BaseResponseEntity<DeclarationRegistersResponse> getDataById(Long id, boolean isAdmin);

    ListResponseEntity<DeclarationRegistersEntity> getDataByProperties(DeclarationRegistersDTO dto, boolean isAdmin);

    ResponseEntity<Object> getImportTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file, HttpServletRequest req);

    void autoRegister();
}
