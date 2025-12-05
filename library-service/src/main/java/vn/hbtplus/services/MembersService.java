/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.BaseSearchRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.models.request.MembersRequest;

/**
 * Lop interface service ung voi bang lib_members
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface MembersService {

    TableResponseEntity<MembersResponse.SearchResult> searchData(MembersRequest.SearchForm dto);

    ResponseEntity saveData(MembersRequest.SubmitForm dto, Long memberId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<MembersResponse.DetailBean> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(MembersRequest.SearchForm dto) throws Exception;

    BaseDataTableDto getPageable(BaseSearchRequest request);
}
