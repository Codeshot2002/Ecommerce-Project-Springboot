package com.ecommerce.project.service;

import com.ecommerce.project.models.Product;
import com.ecommerce.project.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts(Long id, Long categoryId) {
        if(id != null){
            Optional<Product> product = productRepository.findById(id);
            return product.map(List::of).orElseGet(List::of);
        }else if(categoryId != null){
            return productRepository.findByCategoryId(categoryId);
        } else{
            return productRepository.findAll();
        }
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateProduct(Product product, long id) {
        Optional<Product> oldProduct = productRepository.findById(id);
        if(oldProduct.isPresent()) {
            Product newProduct = oldProduct.get();
            if (product.getName() != null && !Objects.equals(product.getName(), newProduct.getName())) {
                newProduct.setName(product.getName());
            } else if(product.getCategoryId() != null && !Objects.equals(product.getCategoryId(), newProduct.getCategoryId())){
                newProduct.setCategoryId(product.getCategoryId());
            } else if(product.getQuantity() != null && !Objects.equals(product.getQuantity(), newProduct.getQuantity())){
                newProduct.setQuantity(product.getQuantity());
            }
            else {
                return new ResponseEntity<>("No fields Updated", HttpStatus.OK);
            }
            productRepository.save(newProduct);
            return new ResponseEntity<>("Product updated!",HttpStatus.OK);
        } else{
            return new ResponseEntity<>("No products exists with that Id", HttpStatus.OK);
        }
    }

    @Override
    public ResponseEntity<String> deleteProduct(long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return new ResponseEntity<>("Product deleted!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No products exists with that Id", HttpStatus.OK);
        }
    }
}
