package com.ecommerce.project.service;

import com.ecommerce.project.models.Product;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    public List<Product> getAllProducts(Long id, Long categoryId);
    public Product addProduct(Product product);
    public ResponseEntity<String> updateProduct(Product product,  long id);
    public ResponseEntity<String> deleteProduct(long id);
}
