package com.security.rest.model;

import io.swagger.models.auth.In;

import javax.persistence.*;
import java.util.*;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "cart")
    private Set<CartProductQuantityTable> cartProductQuantityTableSet = new HashSet<>();

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<CartProductQuantityTable> getCartProductQuantityTableSet() {
        return cartProductQuantityTableSet;
    }

    public void setCartProductQuantityTableSet(Set<CartProductQuantityTable> cartProductQuantityTableSet) {
        this.cartProductQuantityTableSet = cartProductQuantityTableSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return id == cart.id;
    }

}
