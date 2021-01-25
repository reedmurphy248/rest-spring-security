package com.security.rest.controller;

import com.security.rest.model.Product;
import com.security.rest.model.User;
import com.security.rest.repository.ProductRepository;
import com.security.rest.repository.UserRepository;
import com.security.rest.service.JwtUtilService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Spy
    HttpServletRequest request;

    @Mock
    ProductRepository productRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    JwtUtilService jwtUtilService;

    @InjectMocks
    UserController userController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

    }

    @Test
    void hello() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/user"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(content().string("Hello from User Page"));

    }

    @Test
    void getAllProducts() throws Exception {

        List<Product> products = new ArrayList<>();
        products.add(new Product());

        Mockito.when(productRepository.findAll()).thenReturn(products);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/all-products"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(productRepository).findAll();
    }

    @Test
    void addToCart() throws Exception{

        Long id = 1L;

        Product product = new Product("Shoes", "Cool shoes", 72.99);

        Mockito.when(jwtUtilService.getLoggedInUser(request)).thenReturn(new User("test", "Reed", "Murphy",
                "ADMIN", "rmurphy@email.com", "12345"));

        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(product));

        mockMvc.perform(MockMvcRequestBuilders.post("/user/add-to-cart/{productId}", 1))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(content().string("Product Added to Cart"));

    }
}