package com.security.rest.model;

public class NewProductRequest {

    private String name;
    private String description;
    private double unitPrice;

    public NewProductRequest(String name, String description, double unitPrice) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
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
