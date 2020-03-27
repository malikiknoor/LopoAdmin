package com.iknoortech.lopoadmin.model.main;

public class MainTableList {

    private String title;

    private int logo;

    public MainTableList(String title, int logo) {
        this.title = title;
        this.logo = logo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLogo() {
        return logo;
    }

    public void setLogo(int logo) {
        this.logo = logo;
    }
}
