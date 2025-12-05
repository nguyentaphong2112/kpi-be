/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.ResearchProjectsRequest;
import vn.hbtplus.models.response.ResearchProjectsResponse;
import vn.hbtplus.models.response.TableResponseEntity;

/**
 * Lop interface service ung voi bang lms_research_projects
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ResearchProjectsService {

    TableResponseEntity<ResearchProjectsResponse.SearchResult> searchData(ResearchProjectsRequest.SearchForm dto);

    ResponseEntity saveData(ResearchProjectsRequest.SubmitForm dto, Long id) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    ResearchProjectsResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ResearchProjectsRequest.SearchForm dto) throws Exception;

}
