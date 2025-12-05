package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.dto.CategoryTypeDto;
import vn.kpi.models.request.CategoryTypeRequest;
import vn.kpi.models.response.CategoryTypeResponse;

import java.util.List;

public interface CategoryTypeService {
    List<CategoryTypeDto> getListCategoryType(String groupType);

    CategoryTypeDto getCategoryType(String categoryType);

    BaseDataTableDto<CategoryTypeResponse.SearchResult> searchData(CategoryTypeRequest.SearchForm dto);

    CategoryTypeResponse.DetailBean getDataById(Long id);

    ResponseEntity saveData(CategoryTypeRequest.SubmitForm dto, Long categoryTypeId);

    ResponseEntity<Object> exportData(CategoryTypeRequest.SearchForm dto) throws Exception;

    ResponseEntity deleteData(Long id);
}

