package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Represents an embeddable Address object. An embeddable class is not an entity itself but is
 * intended to be embedded within other entities. Its fields will be stored as columns in the table
 * of the owning entity. This promotes reuse and a more organized domain model.
 */
@Embeddable
public class Address {
    private String street;
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    // Getters and Setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
