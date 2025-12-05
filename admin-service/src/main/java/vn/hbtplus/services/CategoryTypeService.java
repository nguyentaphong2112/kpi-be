package vn.hbtplus.services;

import com.jxcell.CellException;
import org.springframework.http.ResponseEntity;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.models.request.CategoryTypeRequest;
import vn.hbtplus.models.response.CategoryResponse;
import vn.hbtplus.models.response.CategoryTypeResponse;

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

