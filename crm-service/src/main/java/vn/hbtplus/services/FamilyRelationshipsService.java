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
import vn.hbtplus.models.request.FamilyRelationshipsRequest;

import java.util.List;

/**
 * Lop interface service ung voi bang crm_family_relationships
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface FamilyRelationshipsService {

    TableResponseEntity<FamilyRelationshipsResponse> searchData(FamilyRelationshipsRequest.SearchForm dto);

    ResponseEntity saveData(FamilyRelationshipsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<FamilyRelationshipsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(FamilyRelationshipsRequest.SearchForm dto) throws Exception;

    void saveData(List<FamilyRelationshipsRequest.SubmitForm> dto, Long objectId, String objName, String ObjectType) throws BaseAppException;

    void processImportData(String type, MultipartFile file) throws Exception;

    ResponseEntity<Object> downloadTemplate() throws Exception;

}
