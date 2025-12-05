/*
 * Copyright (C) 2022 EcoIT. All rights reserved.
 * EcoIT. Use is subject to license terms.
 */
package vn.hbtplus.tax.personal.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import vn.hbtplus.tax.personal.models.request.AdminSearchDTO;
import vn.hbtplus.tax.personal.models.request.InvoiceRequestsDTO;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.tax.personal.models.response.InvoiceRequestsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * Lop interface service ung voi bang PTX_INVOICE_REQUESTS
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface InvoiceRequestsService {

    TableResponseEntity<InvoiceRequestsResponse> searchData(AdminSearchDTO dto);

    ResponseEntity<Object> exportData(AdminSearchDTO dto);

    ResponseEntity<Object> saveData(InvoiceRequestsDTO dto, boolean isAdmin);

    ResponseEntity<Object> deleteData(Long id, boolean isAdmin);

    BaseResponseEntity<InvoiceRequestsResponse> getDataById(Long id, boolean isAdmin);

    ResponseEntity<Object> getImportTemplate() throws Exception;

    ResponseEntity<Object> importProcess(MultipartFile file, HttpServletRequest req);

    void autoRegister();

    void scanInvoiceStatus();

    ResponseEntity<Object> requestExportInvoiceByList(List<Long> listId);

    ResponseEntity<Object> requestExportInvoiceByForm(AdminSearchDTO dto);
}
