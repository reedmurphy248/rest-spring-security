package com.security.rest.repository;

import com.security.rest.model.Cart;
import com.security.rest.model.CartProductQuantityTable;

import com.security.rest.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartProductQuantityTableRepository extends JpaRepository<CartProductQuantityTable, Long> {

    List<CartProductQuantityTable> getCartProductQuantityTablesByCart(Cart cart);

    List<CartProductQuantityTable> getCartProductQuantityTablesByProduct(Product product);

}
