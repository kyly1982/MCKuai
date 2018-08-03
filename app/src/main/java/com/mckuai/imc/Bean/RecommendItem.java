package com.mckuai.imc.Bean;

import android.text.TextUtils;

/**
 * Created by kyly on 2017/6/21.
 */

public class RecommendItem {
    public int id, userId;
    public String name, kinds, insertTime, icon, headImg, userName, url;
    public static final int TYPE_POST = 0, TYPE_RAIDERS = 1, TYPE_AD = 2;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKinds() {
        return kinds;
    }

    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RecommendItem() {
    }

    public RecommendItem(RecommendAd ad) {
        this.id = 0;
        this.headImg = ad.getImageUrl();
        this.name = ad.getViewName();
        this.url = ad.getDownUrl();
    }

    public int getItemType() {
        if (0 == id) {
            return TYPE_AD;
        } else if (0 == userId) {
            return TYPE_RAIDERS;
        } else return TYPE_POST;
    }

    public boolean isDaShengVideo() {
        return ((0 != id) && 0 != userId && kinds.equals(userName));
    }


    public boolean hasCover() {
        return !TextUtils.isEmpty(icon);
    }
}
