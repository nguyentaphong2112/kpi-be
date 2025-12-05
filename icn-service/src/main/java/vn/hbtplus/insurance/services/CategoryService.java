package vn.hbtplus.insurance.services;

import vn.hbtplus.insurance.models.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getCategories(String categoryType);
}
