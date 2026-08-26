package com.ecommerce.project.controllers;

import com.ecommerce.project.models.Category;
import com.ecommerce.project.dto.UpdateCategoryRequest;
import com.ecommerce.project.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import jakarta.validation.Valid;

@RestController
public class CategoryController {
    private CategoryService service;

    public CategoryController(CategoryService categoryService) {
        this.service = categoryService;
    }

    @GetMapping("/api/public/categories")
    public List<Category> getAllCategories() {
       return service.getAllCategories();
    }

    @PostMapping("/api/public/categories")
    public Category createCategory(@RequestBody Category category) {
        return service.addCategory(category);
    }

    @DeleteMapping("/api/public/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable long id) {
        try{
            String response = service.deleteCategory(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
        }
    }

    @PatchMapping("/api/public/categories/{id}")
    public Category updateCategory(@PathVariable long id,
                                   @Valid @RequestBody UpdateCategoryRequest category) {
        return service.updateCategory(id, category);
    }
}
