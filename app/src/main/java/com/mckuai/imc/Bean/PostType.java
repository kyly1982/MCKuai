package com.mckuai.imc.Bean;

import java.io.Serializable;

/**
 * Created by kyly on 2016/1/22.
 */
public class PostType implements Serializable {
    private int id;
    private int smallId;
    private String smallName;

    public PostType() {
    }

    public PostType(int id, int smallId, String smallName) {
        this.id = id;
        this.smallId = smallId;
        this.smallName = smallName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSmallId() {
        return smallId;
    }

    public void setSmallId(int smallId) {
        this.smallId = smallId;
    }

    public String getSmallName() {
        return smallName;
    }

    public void setSmallName(String smallName) {
        this.smallName = smallName;
    }
}
