package vn.hbtplus.insurance.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.insurance.constants.Constant;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.insurance.models.response.CategoryResponse;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.insurance.services.CategoryService;
import vn.hbtplus.utils.ResponseUtils;

@RestController
@RequestMapping("/v1/category")
@RequiredArgsConstructor
@Resource(value = Constant.RESOURCE.ICN_CONFIG_PARAMETER)
public class CategoryController {
    private final CategoryService categoryService;
    @GetMapping(value = "/{categoryType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @HasPermission(scope = Scope.VIEW)
    public ListResponseEntity<CategoryResponse> getCategories(@PathVariable String categoryType) {
        return ResponseUtils.ok(categoryService.getCategories(categoryType));
    }
}
