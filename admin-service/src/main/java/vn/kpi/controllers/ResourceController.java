/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.TreeDto;
import vn.kpi.models.request.ResourceRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.ResourceResponse;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.ResourceService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.RESOURCE)
public class ResourceController {
    private final ResourceService resourcesService;

    @GetMapping(value = "/v1/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<ResourceResponse.SearchResult> searchData(ResourceRequest.SearchForm dto) {
        return ResponseUtils.ok(resourcesService.searchData(dto));
    }

    @PostMapping(value = "/v1/resource", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid  ResourceRequest.SubmitForm dto) throws BaseAppException {
        return ResponseUtils.ok(resourcesService.saveData(dto, null));
    }
    @PutMapping(value = "/v1/resource/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@Valid  ResourceRequest.SubmitForm dto, @PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(resourcesService.saveData(dto, id));
    }

    @DeleteMapping(value = "/v1/resource/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
        return ResponseUtils.ok(resourcesService.deleteData(id));
    }

    @GetMapping(value = "/v1/resource/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<ResourceResponse.DetailBean> getDataById(@PathVariable Long id)  throws RecordNotExistsException {
        return ResponseUtils.ok(resourcesService.getDataById(id));
    }
    @PutMapping(value = "/v1/resource/lock-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity lockById(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(resourcesService.lockById(id));
    }
    @PutMapping(value = "/v1/resource/unlock-by-id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public BaseResponseEntity unlockById(@PathVariable Long id) throws BaseAppException {
        return ResponseUtils.ok(resourcesService.unlockById(id));
    }

    @GetMapping(value = "/v1/resource/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(ResourceRequest.SearchForm dto) throws Exception {
        return resourcesService.exportData(dto);
    }


    @GetMapping(value = "/v1/resource-tree/root-nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> getResourceRootNodes()  {
        return ResponseUtils.ok(resourcesService.getResourceRootNodes());
    }

    @GetMapping(value = "/v1/resource-tree/children-nodes/{nodeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> getResourceChildNodes(@PathVariable String nodeId)  {
        return ResponseUtils.ok(resourcesService.getResourceChildNodes(Long.valueOf(nodeId)));
    }

    @GetMapping(value = "/v1/resource-tree/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<ResourceResponse.SearchTreeChooseResult> search(ResourceRequest.TreeSearchRequest request)  {
        return ResponseUtils.ok(resourcesService.search(request));
    }

    @GetMapping(value = "/v1/resource-tree/init-tree", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<TreeDto> initTree()  {
        return ResponseUtils.ok(resourcesService.initTree());
    }
}
