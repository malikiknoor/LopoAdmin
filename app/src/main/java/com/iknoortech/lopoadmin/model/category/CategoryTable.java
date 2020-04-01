package com.iknoortech.lopoadmin.model.category;

import java.io.Serializable;

public class CategoryTable implements Serializable {

    private String categoryId;

    private String image;

    private String name;

    private String status;

    public CategoryTable() {
    }

    public CategoryTable(String categoryId, String image, String name, String status) {
        this.categoryId = categoryId;
        this.image = image;
        this.name = name;
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
