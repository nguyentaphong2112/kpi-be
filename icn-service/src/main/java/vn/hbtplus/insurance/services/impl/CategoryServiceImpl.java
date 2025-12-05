package vn.hbtplus.insurance.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hbtplus.insurance.models.response.CategoryResponse;
import vn.hbtplus.insurance.repositories.impl.CategoryRepository;
import vn.hbtplus.insurance.services.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    @Override
    public List<CategoryResponse> getCategories(String categoryType) {
        return categoryRepository.getCategories(categoryType);
    }
}
