package com.ecommerce.project.service;

import com.ecommerce.project.models.Category;

import java.util.List;

public interface CategoryService {
    public List<Category> getAllCategories();
    public Category addCategory(Category category);
    public String deleteCategory(long id);

    public Category updateCategory(long id, Category category);
}
