package com.security.rest.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private User user;

    // Maybe Cascade type DETACH
    @ManyToMany(cascade = CascadeType.DETACH)
    @JoinTable(name = "product_cart", joinColumns = @JoinColumn(name = "cart_id"),
    inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products = new HashSet<>();

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    public Cart(User user, Set<Product> products) {
        this.user = user;
        this.products = products;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

}
