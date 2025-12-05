package vn.kpi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.kpi.annotations.HasPermission;
import vn.kpi.annotations.Resource;
import vn.kpi.constants.BaseConstants;
import vn.kpi.constants.Constant;
import vn.kpi.constants.Scope;
import vn.kpi.models.dto.CategoryTypeDto;
import vn.kpi.models.request.CategoryTypeRequest;
import vn.kpi.models.response.BaseResponseEntity;
import vn.kpi.models.response.CategoryTypeResponse;
import vn.kpi.models.response.ListResponseEntity;
import vn.kpi.models.response.TableResponseEntity;
import vn.kpi.services.CategoryTypeService;
import vn.kpi.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCES.CATEGORY_TYPE)
public class CategoryTypeController {
    private final CategoryTypeService categoryTypeService;

    @GetMapping(value = "/v1/category-type", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public TableResponseEntity<CategoryTypeResponse.SearchResult> searchData(CategoryTypeRequest.SearchForm dto) {
        return ResponseUtils.ok(categoryTypeService.searchData(dto));
    }

    @DeleteMapping(value = "/v1/category-type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.DELETE)
    public ResponseEntity deleteData(@PathVariable Long id) {
        return ResponseUtils.ok(categoryTypeService.deleteData(id));
    }

    @GetMapping(value = "/v1/category-type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public BaseResponseEntity<CategoryTypeResponse.DetailBean> getDataById(@PathVariable Long id) {
        return ResponseUtils.ok(categoryTypeService.getDataById(id));
    }

    @PostMapping(value = "/v1/category-type", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.CREATE)
    public ResponseEntity saveData(@RequestBody @Valid CategoryTypeRequest.SubmitForm dto) {
        return categoryTypeService.saveData(dto, null);
    }

    @PutMapping(value = "/v1/category-type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.UPDATE)
    public ResponseEntity updateData(@RequestBody @Valid CategoryTypeRequest.SubmitForm dto, @PathVariable Long id) {
        return categoryTypeService.saveData(dto, id);
    }

    @GetMapping(value = "/v1/category-type/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ResponseEntity<Object> exportData(CategoryTypeRequest.SearchForm dto) throws Exception {
        return categoryTypeService.exportData(dto);
    }

    @GetMapping(value = "/v1/category-type/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponseEntity<CategoryTypeDto> getListCategoryType(@RequestParam(required = false) String groupType) {
        return ResponseUtils.ok(categoryTypeService.getListCategoryType(groupType));
    }

    @GetMapping(value = "/v1/category-type/get-by-code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponseEntity<CategoryTypeDto> getCategoryTypeByCode(@PathVariable String code) {
        return ResponseUtils.ok(categoryTypeService.getCategoryType(code));
    }
}
