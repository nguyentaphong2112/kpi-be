/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.ValidateResponseDto;
import vn.hbtplus.tax.income.models.request.TaxDeclareMastersRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.tax.income.models.response.TaxDeclareMastersResponse;
import vn.hbtplus.tax.income.repositories.entity.TaxDeclareMastersEntity;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Lop interface service ung voi bang pit_tax_declare_masters
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface TaxDeclareMastersService {

    TableResponseEntity<TaxDeclareMastersResponse> searchData(TaxDeclareMastersRequest.SearchForm dto);

    ResponseEntity saveData(TaxDeclareMastersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException, BaseAppException;

    BaseResponseEntity<TaxDeclareMastersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(TaxDeclareMastersRequest.SearchForm dto) throws Exception;

    ResponseEntity exportDetailKK02(Long id, List<Long> orgIds) throws Exception;

    ResponseEntity<Object> exportTaxAllocation(Long id, List<Long> orgIds) throws Exception;

    void lockPeriodById(Long id) throws RecordNotExistsException, BaseAppException;

    void unLockPeriodById(Long id) throws RecordNotExistsException, BaseAppException;

    ResponseEntity downloadTemplate(Date taxPeriodDate) throws Exception;

    Long importTaxDeclare(MultipartFile fileImport, Date taxPeriodDate) throws ErrorImportException, IOException, ExecutionException, InterruptedException;

    Long calculate(Date taxPeriodDate) throws BaseAppException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException, ParseException;

    ValidateResponseDto validateCalculate(Date taxPeriodDate, String inputType) throws BaseAppException;

    ResponseEntity<Object> exportXml(Long id, List<Long> orgIds) throws IOException, SAXException, ParserConfigurationException, TransformerException;

    TaxDeclareMastersEntity getTaxDeclareMaster(Date taxPeriodDate, String inputType) ;
}
