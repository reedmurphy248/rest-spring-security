package com.security.rest.controller;

import com.security.rest.exception.ProductDoesntExistException;
import com.security.rest.model.CartProductQuantityTable;
import com.security.rest.model.NewProductRequest;
import com.security.rest.model.Product;
import com.security.rest.model.ProductResponse;
import com.security.rest.repository.CartProductQuantityTableRepository;
import com.security.rest.repository.ProductRepository;
import com.security.rest.security.JwtRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "https://react-security-product-app.herokuapp.com/"})
@RestController
public class AdminController {

    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartProductQuantityTableRepository cartProductQuantityTableRepository;

    @GetMapping(value = "/admin")
    public String hello() {
        return "Hello from Admin Page";
    }

    @GetMapping(value = "/admin/product/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable(name = "productId") Long id) {

        if (productRepository.findById(id).isPresent()) {

            Product foundProduct = productRepository.findById(id).get();

            ProductResponse productResponse = new ProductResponse(foundProduct.getId(), foundProduct.getName(),
                    foundProduct.getDescription(), foundProduct.getUnitPrice());

            return ResponseEntity.ok(productResponse);

        } else throw new ProductDoesntExistException("Product Doesn't Exist");

    }

    @PostMapping(value = "/admin/add-product")
    public ResponseEntity<String> addNewProduct(@RequestBody NewProductRequest newProductRequest) {

        Product newProduct = new Product(newProductRequest.getName(), newProductRequest.getDescription(), newProductRequest.getUnitPrice());
        productRepository.save(newProduct);
        return new ResponseEntity<>("Product Successfully Created", HttpStatus.OK);

    }

    @PostMapping(value = "/admin/{productId}")
    public ResponseEntity<String> updateProduct(@RequestBody NewProductRequest newProductRequest, @PathVariable(name = "productId") Long id) {

        if (productRepository.findById(id).isPresent()) {
            Product productToUpdate = productRepository.findById(id).get();

            productToUpdate.setName(newProductRequest.getName());
            productToUpdate.setDescription(newProductRequest.getDescription());
            productToUpdate.setUnitPrice(newProductRequest.getUnitPrice());

            productRepository.save(productToUpdate);

            return new ResponseEntity<>("Product Updated", HttpStatus.OK);

        } else {
            throw new ProductDoesntExistException("Product Doesn't Exist");
        }

    }

    @PostMapping(value = "/admin/delete-product/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable(name = "productId") Long id) {

        Optional<Product> searchedProduct = productRepository.findById(id);

        if (searchedProduct.isPresent()) {

            Product productResult = searchedProduct.get();

            List<CartProductQuantityTable> cartProductQuantityTableList = cartProductQuantityTableRepository.getCartProductQuantityTablesByProduct(productResult);

            cartProductQuantityTableList.forEach(cartProductQuantityTable -> {
                cartProductQuantityTableRepository.delete(cartProductQuantityTable);
            });

            productRepository.delete(productResult);

            return ResponseEntity.ok("Product Deleted");

        } else throw new ProductDoesntExistException("Product Doesn't Exist");

    }

}

