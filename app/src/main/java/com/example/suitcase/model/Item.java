package com.example.suitcase.model;

import java.io.Serializable;

public class Item {
    String id;
    private String name;
    private String description;
    private String price;
    private String imageUrl;
    private boolean isPurchased;
    private boolean isTagged;
    private double latTag;
    private double lonTag;

    public Item() {
    }

    public Item(String name, String description, String price, String imageUrl, boolean isPurchased) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isPurchased = isPurchased;
    }

    public Item(String name, String description, String price, String imageUrl, boolean isPurchased, boolean isTagged,
                double latTag, double lonTag) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isPurchased = isPurchased;
        this.isTagged = isTagged;
        this.latTag = latTag;
        this.lonTag = lonTag;
    }

    public Item(String itemId, String itemName, String itemDescription, String price, String imageUrl) {

    }

    public Item(String itemName, String itemDescription, double price, String toString) {
    }

    public Item(String itemId, String imageUrl) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPurchased() {
        return isPurchased;
    }

    public void setPurchased(boolean purchased) {
        isPurchased = purchased;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean tagged) {
        isTagged = tagged;
    }

    public double getLatTag() {
        return latTag;
    }

    public void setLatTag(double latTag) {
        this.latTag = latTag;
    }

    public double getLonTag() {
        return lonTag;
    }

    public void setLonTag(double lonTag) {
        this.lonTag = lonTag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}