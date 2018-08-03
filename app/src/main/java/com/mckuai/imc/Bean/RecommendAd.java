package com.mckuai.imc.Bean;

public class RecommendAd {
    private String downUrl;
    private String imageUrl;
    private String viewName;

    public RecommendAd(String downUrl, String imageUrl, String viewName) {
        this.downUrl = downUrl;
        this.imageUrl = imageUrl;
        this.viewName = viewName;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
}
