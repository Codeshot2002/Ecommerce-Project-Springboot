package com.ecommerce.project.controllers;

import com.ecommerce.project.models.Product;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/api/public/products")
    public List<Product> getAllProducts (@RequestParam(required = false) Long id, @RequestParam(required = false) Long categoryId) {
        return productService.getAllProducts(id, categoryId);
    }

    @PostMapping("/api/admin/products")
    public Product addProduct(@Valid @RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PatchMapping("/api/admin/products")
    public ResponseEntity<String> updateProduct(@RequestBody Product product, @RequestParam long id) {
        return productService.updateProduct(product,id);
    }

    @DeleteMapping("/api/admin/products")
    public ResponseEntity<String> deleteProduct(@RequestParam long id) {
        return productService.deleteProduct(id);
    }
}
