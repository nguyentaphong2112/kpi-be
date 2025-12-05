/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.TreeDto;
import vn.hbtplus.models.request.ResourceRequest;
import vn.hbtplus.models.response.ResourceResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_resources
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface ResourceService {

    BaseDataTableDto<ResourceResponse.SearchResult> searchData(ResourceRequest.SearchForm dto);

    Long saveData(ResourceRequest.SubmitForm dto, Long resourceId) throws BaseAppException;

    boolean deleteData(Long id) throws RecordNotExistsException;

    ResourceResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(ResourceRequest.SearchForm dto) throws Exception;

    boolean lockById(Long id) throws BaseAppException;

    boolean unlockById(Long id) throws BaseAppException;

    List<TreeDto> getResourceRootNodes();

    List<TreeDto> getResourceChildNodes(Long nodeId);

    List<TreeDto> initTree();

    BaseDataTableDto<ResourceResponse.SearchTreeChooseResult> search(ResourceRequest.TreeSearchRequest request);
}
