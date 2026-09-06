package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartItem;
import com.ecommerce.project.dto.CartRequest;
import com.ecommerce.project.dto.CartResponse;
import com.ecommerce.project.models.Product;
import com.ecommerce.project.models.User;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    public static final String REDIS_CART_KEY = "cart:user:";
    private final UserRepository userRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ProductRepository productRepository;

    public CartServiceImpl(UserRepository userRepository, RedisTemplate<Object, Object> redisTemplate, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    @Override
    public void addToCart(String email, CartRequest cartRequest) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        String cartKey = getCartKey(email, user);
        Product product = productRepository.findById(cartRequest.productId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (product.getQuantity() > cartRequest.quantity()) {
            redisTemplate.opsForHash().put(cartKey, cartRequest.productId(), cartRequest.quantity());
            redisTemplate.expire(cartKey, Expiration.from(24, TimeUnit.HOURS));
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantity exceeds the inventory");
        }
    }

    @Override
    public CartResponse getCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        String cartKey = getCartKey(email, user);
        Map<Object, Object> cart = redisTemplate.opsForHash().entries(cartKey);
        if (cart == null || cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart is empty");
        }
        Map<Long, Product> products = productRepository.findAllById(cart.keySet().stream().map(x -> (Long) x).toList()).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        List<CartItem> cartItems = new ArrayList<>();
        Long totalPrice = 0L;
        for (Map.Entry<Object, Object> entry : cart.entrySet()) {
            Product product = products.get((Long) entry.getKey());
            Integer quantity = (Integer) entry.getValue();
            Integer productPrice = 0; // Price of the product will be added here when supported
            Long totalProductPrice = (long) (productPrice * quantity);
            if (product == null) {
                cartItems.add(new CartItem(null, null, quantity, totalProductPrice, "Product not found"));
            } else {
                cartItems.add(new CartItem(product.getId(), product.getName(), quantity, totalProductPrice, null));
            }
            totalPrice += totalProductPrice;
        }
        return new CartResponse(cartItems, totalPrice);
    }

    @Override
    public String removeCartItem(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        Optional<Product> product = productRepository.findById(productId);
        String cartKey = getCartKey(email, user);
        redisTemplate.opsForHash().delete(cartKey, productId);
        if (product.isPresent()) {
            return product.get().getName();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    private String getCartKey(String email, User user) {
        return REDIS_CART_KEY + user.getId();
    }
}
