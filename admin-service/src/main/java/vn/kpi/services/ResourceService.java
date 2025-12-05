/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.TreeDto;
import vn.kpi.models.request.ResourceRequest;
import vn.kpi.models.response.ResourceResponse;

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
