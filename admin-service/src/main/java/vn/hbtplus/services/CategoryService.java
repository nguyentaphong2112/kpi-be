/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.request.CategoryRequest;
import vn.hbtplus.models.response.CategoryResponse;

import java.util.List;

/**
 * Lop interface service ung voi bang sys_categories
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

public interface CategoryService {

    BaseDataTableDto<CategoryResponse.SearchResult> searchData(String categoryType, CategoryRequest.SearchForm dto);

    ResponseEntity saveData(String categoryType,CategoryRequest.SubmitForm dto, Long categoryId) throws BaseAppException;

    ResponseEntity deleteData(Long id) throws RecordNotExistsException;

    CategoryResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    ResponseEntity<Object> exportData(String categoryType,CategoryRequest.SearchForm dto) throws Exception;

    List<CategoryDto> getListCategories(String categoryType, boolean isGetAttribute, String keyAttribute, boolean isActive);
    List<CategoryDto> getListCategories(String categoryType,List<String>  ids, boolean isGetAttribute);

    BaseDataTableDto<CategoryDto> getPageable(String categoryType, CategoryRequest.PageableRequest request);
    BaseDataTableDto<CategoryDto> getPageableKeyCheck(String categoryType, CategoryRequest.PageableRequest request);

    List<CategoryDto> getListDistrict(boolean isGetAttribute);

    List<CategoryDto> getListDistrictByProvince(String provinceId, boolean isGetAttribute);

    List<CategoryDto> getListWardByDistrict(String districtId, boolean isGetAttribute);

    List<CategoryDto> getListCategoriesByParent(String categoryType, String parentTypeCode, String parentValue, boolean isGetAttribute);

    List<CategoryDto> getListWardByProvince(String provinceId, boolean isGetAttribute);

    CategoryResponse.DetailBean getDataByValue(String categoryType, String value);
}
