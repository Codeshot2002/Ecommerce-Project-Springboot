package com.ecommerce.project.service;

import com.ecommerce.project.models.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    List<Category> categories = new ArrayList<>();
    private long id = 1L;

    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public Category addCategory(Category category) {
        category.setId(id++);
        categories.add(category);
        return category;
    }

    @Override
    public String deleteCategory(long id) {
        if(categories.removeIf(category -> category.getId() == id)){
            return String.valueOf(id);
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id of the category doesn't exist");
        }
    }

    @Override
    public Category updateCategory(long id, Category category) {
        Optional<Category> obj = categories.stream().filter(category1 -> category1.getId() == id).findFirst();
        if(obj.isPresent()){
            Category updatedCategory = obj.get();
            updatedCategory.setName(category.getName());
            updatedCategory.setDescription(category.getDescription());
            return updatedCategory;
        } else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Id of the category doesn't exist");
        }
    }

}
