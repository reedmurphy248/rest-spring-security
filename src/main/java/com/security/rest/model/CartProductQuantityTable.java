package com.security.rest.model;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
public class CartProductQuantityTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private double quantity;

    public CartProductQuantityTable() {}

    public CartProductQuantityTable(double quantity) {
        this.quantity = quantity;
    }

    public CartProductQuantityTable(Cart cart, Product product, double quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartProductQuantityTable that = (CartProductQuantityTable) o;
        return Double.compare(that.quantity, quantity) == 0 && Id.equals(that.Id) && cart.equals(that.cart) && product.equals(that.product);
    }

}
