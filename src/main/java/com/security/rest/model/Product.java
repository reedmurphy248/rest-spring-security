package com.security.rest.model;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String name;
    private String description;
    private double unitPrice;

    @OneToMany(mappedBy = "product")
    private Set<CartProductQuantityTable> cartProductQuantityTableSet;

    public Product(){}

    public Product(String name, String description, double unitPrice) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
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
        Product product = (Product) o;
        return id.equals(product.id);
    }

}
