package com.ecommerce.project.service;

import com.ecommerce.project.dto.UpdateCategoryRequest;
import com.ecommerce.project.models.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(Category category) {

        categoryRepository.save(category);
        return category;
    }

    @Override
    public String deleteCategory(long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return "Category with [" + id + "] has been deleted";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id of the category doesn't exist");
        }
    }

    @Override
    @Transactional
    public Category updateCategory(long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found: " + id));

        if (request.name() == null && request.description() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one updatable field must be supplied");
        }

        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }

        return category;
    }

}
