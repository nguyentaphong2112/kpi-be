/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.CategoryRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.CategoryResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.services.CategoryService;
import vn.hbtplus.services.RedisService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoriesService;
    private final RedisService redisService;

    @GetMapping(value = "/v1/category/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CategoryResponse.SearchResult> searchData(CategoryRequest.SearchForm dto, @PathVariable String categoryType) {
        return ResponseUtils.ok(categoriesService.searchData(categoryType, dto));
    }

    @GetMapping(value = "/v1/category/list/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListCategories(@PathVariable String categoryType, @RequestParam(required = false) boolean isGetAttribute,
                                                             @RequestParam(required = false) String keyAttribute,
                                                             @RequestParam(required = false) boolean isActive) {
        return ResponseUtils.ok(categoriesService.getListCategories(categoryType, isGetAttribute, keyAttribute, isActive));
    }

    @GetMapping(value = "/v1/category/list-by-parent/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListCategoriesByParent(@PathVariable String categoryType,
                                                                     @RequestParam String parentTypeCode,
                                                                     @RequestParam String parentValue,
                                                                     @RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(categoriesService.getListCategoriesByParent(categoryType, parentTypeCode, parentValue, isGetAttribute));
    }

    @GetMapping(value = "/v1/district/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListDistrict(@RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(categoriesService.getListDistrict(isGetAttribute));
    }

    @GetMapping(value = "/v1/district/list/{provinceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListDistrictByProvince(
            @PathVariable String provinceId,
            @RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(categoriesService.getListDistrictByProvince(provinceId, isGetAttribute));
    }

    @GetMapping(value = "/v1/ward/list/{districtId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListWardByDistrict(
            @PathVariable String districtId,
            @RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(categoriesService.getListWardByDistrict(districtId, isGetAttribute));
    }

    @GetMapping(value = "/v1/ward/list-of-province/{provinceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListWardByProvince(
            @PathVariable String provinceId,
            @RequestParam(required = false) boolean isActive) {
        return ResponseUtils.ok(categoriesService.getListWardByProvince(provinceId, isActive));
    }

    @GetMapping(value = "/v1/category/get-by-ids/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryDto> getListCategories(@PathVariable String categoryType,
                                                             @RequestParam List<String> ids,
                                                             @RequestParam(required = false) boolean isGetAttribute) {
        return ResponseUtils.ok(categoriesService.getListCategories(categoryType, ids, isGetAttribute));
    }

    @GetMapping(value = "/v1/category/pageable/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<CategoryDto> getPageable(@PathVariable String categoryType,
                                                        CategoryRequest.PageableRequest request) {
        return ResponseUtils.ok(categoriesService.getPageable(categoryType, request));
    }

    @GetMapping(value = "/v1/category/pageable-key-check/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableResponseEntity<CategoryDto> getPageable2(@PathVariable String categoryType,
                                                         CategoryRequest.PageableRequest request) {
        return ResponseUtils.ok(categoriesService.getPageableKeyCheck(categoryType, request));
    }

    @PostMapping(value = "/v1/category/{categoryType}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@Valid CategoryRequest.SubmitForm dto, @PathVariable String categoryType) throws BaseAppException {
        return categoriesService.saveData(categoryType, dto, null);
    }

    @PutMapping(value = "/v1/category/{categoryType}/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity update(@Valid CategoryRequest.SubmitForm dto, @PathVariable String categoryType, @PathVariable Long id) throws BaseAppException {
        return categoriesService.saveData(categoryType, dto, id);
    }

    @DeleteMapping(value = "/v1/category/{categoryType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id, @PathVariable String categoryType) throws RecordNotExistsException {
        return categoriesService.deleteData(id);
    }

    @GetMapping(value = "/v1/category/{categoryType}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CategoryResponse.DetailBean> getDataById(@PathVariable Long id, @PathVariable String categoryType) throws RecordNotExistsException {
        return ResponseUtils.ok(categoriesService.getDataById(id));
    }

    @GetMapping(value = "/v1/category/{categoryType}/by-value/{value}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CategoryResponse.DetailBean> getDataByValue(@PathVariable String value, @PathVariable String categoryType) throws RecordNotExistsException {
        return ResponseUtils.ok(categoriesService.getDataByValue(categoryType, value));
    }

    @GetMapping(value = "/v1/category/{categoryType}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CategoryRequest.SearchForm dto, @PathVariable String categoryType) throws Exception {
        return categoriesService.exportData(categoryType, dto);
    }

    @GetMapping(value = "/v1/cache/delete-by-name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteCache(@RequestParam("cacheName") String cacheName,
                                              @RequestParam(required = false) String cacheKey) throws Exception {
        redisService.deleteCacheByName(cacheName, cacheKey);
        return ResponseUtils.ok();
    }

}
