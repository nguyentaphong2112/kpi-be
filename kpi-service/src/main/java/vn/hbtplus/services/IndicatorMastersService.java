/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.IndicatorMastersRequest;

import java.io.IOException;

/**
 * Lop interface service ung voi bang kpi_indicator_masters
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

public interface IndicatorMastersService {

    TableResponseEntity<IndicatorMastersResponse> searchData(IndicatorMastersRequest.SearchForm dto) throws Exception;

    ResponseEntity saveData(IndicatorMastersRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<IndicatorMastersResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(IndicatorMastersRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> exportDataId(Long id) throws Exception;

    ResponseEntity updateStatusByOrg(IndicatorMastersRequest.SubmitForm dto) throws RecordNotExistsException;

    ResponseEntity approvalAll() throws RecordNotExistsException;

    ResponseEntity<Object> importData(IndicatorMastersRequest.ImportRequest dto) throws IOException;

    String getTemplateImport(IndicatorMastersRequest.ImportRequest dto) throws Exception;
}
