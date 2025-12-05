/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.tax.income.models.request.TaxSettlementMastersRequest;
import vn.hbtplus.tax.income.models.response.TaxSettlementMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.TaxSettlementMastersEntity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Lop interface service ung voi bang pit_tax_settlement_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TaxSettlementMastersService {

    TableResponseEntity<TaxSettlementMastersResponse> searchData(TaxSettlementMastersRequest.SearchForm dto);

    ResponseEntity saveData(TaxSettlementMastersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException, BaseAppException;

    BaseResponseEntity<TaxSettlementMastersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TaxSettlementMastersRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate() throws Exception;

    Long importSettlement(MultipartFile fileImport, int year) throws IOException, ErrorImportException, ExecutionException, InterruptedException;

    ResponseEntity<Object> exportDataById(Long id) throws Exception;

    TaxSettlementMastersEntity calculate(Integer year, TaxSettlementMastersRequest.CalculateForm calculateForm) throws ExecutionException, InterruptedException, BaseAppException, InstantiationException, IllegalAccessException;

    void updateStatus(Long id, String status) throws BaseAppException;
    ResponseEntity exportOrgGroupDetailByMasterId(Long id) throws Exception;

    ResponseEntity exportMonth(Long id) throws Exception;
}
