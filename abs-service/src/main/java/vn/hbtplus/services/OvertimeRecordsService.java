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
import vn.hbtplus.models.request.OvertimeRecordsRequest;

/**
 * Lop interface service ung voi bang abs_overtime_records
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface OvertimeRecordsService {

    TableResponseEntity<OvertimeRecordsResponse> searchData(OvertimeRecordsRequest.SearchForm dto);

    ResponseEntity saveData(OvertimeRecordsRequest.SubmitForm dto,Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<OvertimeRecordsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(OvertimeRecordsRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> processImport(MultipartFile file) throws Exception;

    ResponseEntity<Object> downloadImportTemplate() throws Exception;

}
