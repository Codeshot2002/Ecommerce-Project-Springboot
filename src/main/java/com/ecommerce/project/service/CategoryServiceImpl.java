package com.ecommerce.project.service;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    List<Category> categories = new ArrayList<>();
    private long id = 1L;

    @Autowired
    private CategoryRepository categoryRepository;

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
    public Category updateCategory(long id, Category category) {
        Optional<Category> updatedCategory = categoryRepository.findById(id);
        if(updatedCategory.isPresent()){
            Category c = updatedCategory.get();
            c.setName(category.getName());
            c.setDescription(category.getDescription());
            categoryRepository.save(c);
            return c;
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id of the category doesn't exist");
        }
    }

}
