/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.RelatedOrganizationsRequest;

/**
 * Lop interface service ung voi bang hr_related_organizations
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface RelatedOrganizationsService {

    TableResponseEntity<RelatedOrganizationsResponse> searchData(RelatedOrganizationsRequest.SearchForm dto);

    ResponseEntity saveData(RelatedOrganizationsRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<RelatedOrganizationsResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(RelatedOrganizationsRequest.SearchForm dto) throws Exception;

}
