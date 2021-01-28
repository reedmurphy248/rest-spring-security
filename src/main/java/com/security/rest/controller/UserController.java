package com.security.rest.controller;

import com.security.rest.exception.NoValidCartException;
import com.security.rest.exception.ProductDoesntExistException;
import com.security.rest.model.*;
import com.security.rest.repository.CartProductQuantityTableRepository;
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
    private CartProductQuantityTableRepository cartProductQuantityTableRepository;

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

            Set<CartProductQuantityTable> cartProductQuantityTableSet = userCart.getCartProductQuantityTableSet();

            Optional<CartProductQuantityTable> userCartTable = cartProductQuantityTableSet.stream()
            .filter(cartProductQuantityTable -> cartProductQuantityTable.getCart().equals(userCart)
                    && cartProductQuantityTable.getProduct().equals(searchedProduct))
            .findFirst();

            if (userCartTable.isPresent()) {

                CartProductQuantityTable resultTable = userCartTable.get();

                cartProductQuantityTableSet = cartProductQuantityTableSet.stream()
                        .filter(cartProductQuantityTable -> !cartProductQuantityTable.equals(resultTable))
                        .collect(Collectors.toSet());

                double prevQuantity = resultTable.getQuantity();

                resultTable.setQuantity(prevQuantity + 1);

                cartProductQuantityTableSet.add(resultTable);

            } else {

                CartProductQuantityTable savedTable = cartProductQuantityTableRepository.save(new CartProductQuantityTable(userCart,
                        searchedProduct, 1));

                cartProductQuantityTableSet.add(savedTable);

            }

            userCart.setCartProductQuantityTableSet(cartProductQuantityTableSet);

            cartRepository.save(userCart);

            return ResponseEntity.ok("Product Added");

        } else throw new ProductDoesntExistException("Product Doesn't Exist");

    }

    @GetMapping(value = "/user/getCart")
    public ResponseEntity<?> getCart(HttpServletRequest request) {

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        Cart userCart = loggedInUser.getCart();

        if(userCart != null) {

            List<CartProductQuantityTable> cartProductQuantityTableList = cartProductQuantityTableRepository.getCartProductQuantityTablesByCart(userCart);

            List<ProductResponse> responseList = new ArrayList<>();

            cartProductQuantityTableList.forEach(cartProductQuantityTable -> {
                Product product = cartProductQuantityTable.getProduct();
                ProductResponse productResponse = new ProductResponse(product.getId(), product.getName(),
                        product.getDescription(), product.getUnitPrice(), cartProductQuantityTable.getQuantity());
                responseList.add(productResponse);
            });

            return ResponseEntity.ok(responseList);

        } else {

            throw new NoValidCartException("No Valid Cart");

        }

    }

    @PostMapping(value = "/user/remove-from-cart/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable(name = "productId") Long id, HttpServletRequest request){

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        Cart userCart = loggedInUser.getCart();

        Optional<Product> searchedProduct = productRepository.findById(id);

        Set<CartProductQuantityTable> userCartTableSet = userCart.getCartProductQuantityTableSet();

        if (searchedProduct.isPresent()) {

            Set<CartProductQuantityTable> deleteTableSet = userCartTableSet.stream()
                    .filter(cartProductQuantityTable -> cartProductQuantityTable.getProduct().equals(searchedProduct.get()))
                    .collect(Collectors.toSet());

            deleteTableSet.forEach(CartProductQuantityTable -> cartProductQuantityTableRepository.delete(CartProductQuantityTable));

            return ResponseEntity.ok("Product Removed From Cart");

        } else throw new ProductDoesntExistException("Product Doesn't Exist");

    }

    @PostMapping(value = "/user/update-cart/{productId}")
    public ResponseEntity<?> updateCart(@PathVariable(name = "productId") Long id, HttpServletRequest request) {

        User loggedInUser = jwtUtilService.getLoggedInUser(request);

        Cart userCart = loggedInUser.getCart();

        Optional<Product> searchedProduct = productRepository.findById(id);

        if (searchedProduct.isPresent()) {

            Product resultProduct = searchedProduct.get();

            Set<CartProductQuantityTable> userCartTableSet = userCart.getCartProductQuantityTableSet();

            Optional<CartProductQuantityTable> userCartTable = userCartTableSet.stream()
                    .filter(cartProductQuantityTable -> cartProductQuantityTable.getProduct().equals(resultProduct)
                            && cartProductQuantityTable.getCart().equals(userCart))
                    .findFirst();

            if (userCartTable.isPresent()) {

                CartProductQuantityTable resultTable = userCartTable.get();

                userCartTableSet = userCartTableSet.stream()
                        .filter(cartProductQuantityTable -> !cartProductQuantityTable.equals(resultTable))
                        .collect(Collectors.toSet());

                double prevQuantity = resultTable.getQuantity();

                if (prevQuantity > 1) {

                    resultTable.setQuantity(prevQuantity - 1);

                    userCartTableSet.add(resultTable);

                } else {

                    cartProductQuantityTableRepository.delete(resultTable);

                }

                userCart.setCartProductQuantityTableSet(userCartTableSet);

                cartRepository.save(userCart);

            }

            return ResponseEntity.ok("Cart Updated");

        } else throw new ProductDoesntExistException("Product Doesn't Exist");

    }

}
