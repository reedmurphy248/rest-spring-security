package com.security.rest.model;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private double unitPrice;

    public ProductResponse(Long id, String name, String description, double unitPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
