package com.ecommerce.project.service;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    public List<Category> getAllCategories();
    public Category addCategory(Category category);
    public String deleteCategory(long id);

    Category updateCategory(long id, UpdateCategoryRequest category);
}
