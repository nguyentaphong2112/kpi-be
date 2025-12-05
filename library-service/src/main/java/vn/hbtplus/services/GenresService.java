/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.request.GenresRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.GenresResponse;
import vn.hbtplus.models.response.TableResponseEntity;

import java.util.List;

/**
 * Lop interface service ung voi bang lib_genres
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface GenresService {

    TableResponseEntity<GenresResponse> searchData(GenresRequest.SearchForm dto);

    ResponseEntity saveData(GenresRequest.SubmitForm dto) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    BaseResponseEntity<GenresResponse> getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(GenresRequest.SearchForm dto) throws Exception;

    List<TreeDto> initTree();
}
