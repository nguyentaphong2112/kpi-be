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
import vn.hbtplus.models.request.MappingValuesRequest;

/**
 * Lop interface service ung voi bang sys_mapping_values
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface MappingValuesService {

    TableResponseEntity<MappingValuesResponse> searchData(MappingValuesRequest.SearchForm dto, String configMappingCode);

    ResponseEntity saveData(MappingValuesRequest.SubmitForm dto, String configMappingCode, Long id ) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<MappingValuesResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(MappingValuesRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate (String configMappingCode) throws Exception;

    ResponseEntity importData(MultipartFile fileImport, MappingValuesRequest.ImportForm dto,String configMappingCode) throws Exception;

}
