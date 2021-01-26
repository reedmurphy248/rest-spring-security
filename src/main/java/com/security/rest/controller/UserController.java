package com.security.rest.controller;

import com.security.rest.exception.NoValidCartException;
import com.security.rest.exception.ProductDoesntExistException;
import com.security.rest.model.Cart;
import com.security.rest.model.Product;
import com.security.rest.model.ProductResponse;
import com.security.rest.model.User;
import com.security.rest.repository.CartRepository;
import com.security.rest.repository.ProductRepository;
import com.security.rest.repository.UserRepository;
import com.security.rest.security.JwtRequestFilter;
import com.security.rest.service.JwtUtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:3000", "https://react-security-product-app.herokuapp.com/"})
@RestController
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JwtUtilService jwtUtilService;

    @GetMapping(value = "/user")
    public String hello() {
        logger.info("Made it into /user function");

        return "Hello from User Page";
    }

    // FIX THIS IMPL
    @GetMapping(value = "/user/all-products")
    public List<ProductResponse> getAllProducts() {

        // This will work but the O(n2) should be fixed

        List<ProductResponse> productResponseList = new ArrayList<>();

        productRepository.findAll().forEach(product -> {
                    productResponseList.add(new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getUnitPrice()));
                });

        return productResponseList;

    }

    @PostMapping(value = "/user/add-to-cart/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable(name = "productId") Long id, HttpServletRequest request) {

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        if (loggedInUser.getCart() == null) {

            loggedInUser.setCart(new Cart(loggedInUser));

        }

        if (productRepository.findById(id).isPresent()) {

            Product searchedProduct = productRepository.findById(id).get();

            Cart userCart = loggedInUser.getCart();

            userCart.getProducts().add(searchedProduct);

            cartRepository.save(userCart);

            // Don't add, creates circular saved reference to User and Product
//            searchedProduct.getUsers().add(loggedInUser);

//            productRepository.save(searchedProduct);

            userRepository.save(loggedInUser);

            return new ResponseEntity<>("Product Added to Cart", HttpStatus.OK);

        } else {

            throw new ProductDoesntExistException("Product Does Not Exist");

        }

    }

    @GetMapping(value = "/user/getCart")
    public ResponseEntity<?> getCart(HttpServletRequest request) {

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        Cart userCart = loggedInUser.getCart();

        if(userCart != null) {
            Set<Product> products = userCart.getProducts();

            List<ProductResponse> productResponseList = new ArrayList<>();

            products.forEach(product -> {
                productResponseList.add(new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getUnitPrice()));
            });

            return ResponseEntity.ok(productResponseList);

        } else {

            throw new NoValidCartException("No Valid Cart");

        }

    }

    @PostMapping(value = "/user/remove-from-cart/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable(name = "productId") Long id, HttpServletRequest request){

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        Cart userCart = loggedInUser.getCart();

        Set<Product> newProductList = userCart.getProducts().stream()
                .filter(product -> !product.getId().equals(id))
                .collect(Collectors.toSet());

        userCart.setProducts(newProductList);
        cartRepository.save(userCart);

        List<ProductResponse> productResponseList = newProductList.stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getUnitPrice()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(productResponseList);

//        if (productRepository.findById(id).isPresent()) {
//
//            Product searchedProduct = productRepository.findById(id).get();
//
//            loggedInUser.getProductCart().remove(searchedProduct);
//
//            userRepository.save(loggedInUser);
//
//            return new ResponseEntity<>("Product Removed From Cart", HttpStatus.OK);
//
//        } else {
//
//            throw new ProductDoesntExistException("Product Does Not Exist");
//
//        }

    }


}
