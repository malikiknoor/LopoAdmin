package com.iknoortech.lopoadmin.model.category;

public class CategoryTable {

    private String categoryId;

    private String image;

    private String name;

    public CategoryTable() {
    }

    public CategoryTable(String categoryId, String image, String name) {
        this.categoryId = categoryId;
        this.image = image;
        this.name = name;
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
}
