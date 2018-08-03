package com.mckuai.imc.Bean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

import java.io.Serializable;

/**
 * Entity mapped to table "Users".
 */
public class User implements Serializable {

    private Long id;
    private Integer level;
    private Float process;
    /**
     * Not-null value.
     */
    private String name;
    private String nick;
    private String headImage;
    private Boolean isFriend;
    private String nike;//兼容接口上的字段错误

    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public User(Long id, Integer level, Float process, String name, String nick, String cover, Boolean isFriend) {
        this.id = id;
        this.level = level;
        this.process = process;
        this.name = name;
        this.nick = nick;
        this.headImage = cover;
        this.isFriend = isFriend;
    }

    public User(MCUser mcUser) {
        this.id = (long) mcUser.getId();
        this.level = mcUser.getLevel();
        this.process = mcUser.getProcess();
        this.name = mcUser.getName();
        this.nick = mcUser.getNike();
        this.headImage = mcUser.getHeadImg();
        this.isFriend = false;
    }

    public void clone(MCUser user) {
        this.id = (long) user.getId();
        this.level = user.getLevel();
        this.process = user.getProcess();
        this.name = user.getName();
        this.nick = user.getNike();
        this.headImage = user.getHeadImg();
        this.isFriend = false;
    }

    public void update(User user) {
        if (this.name.equalsIgnoreCase(user.getName())) {
            this.id = user.getId();
            this.level = user.getLevel();
            this.process = user.getProcess();
            //this.name = user.getName();
            this.nick = user.getNick();
            this.headImage = user.getHeadImage();
            this.isFriend = user.getIsFriend();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Float getProcess() {
        return process;
    }

    public void setProcess(Float process) {
        this.process = process;
    }

    /**
     * Not-null value.
     */
    public String getName() {
        return name;
    }

    /**
     * Not-null value; ensure this value is available before it is saved to the database.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getNick() {
        return null != nick ? nick : nike;
    }

    public String getNickEx() {
        if (null != getNick() && getNick().length() > 0 && !getNick().equalsIgnoreCase("null")) {
            return getNick();
        }
        return name;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public Boolean getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(Boolean isFriend) {
        this.isFriend = isFriend;
    }

    public String getNike() {
        return nike;
    }

    public void setNike(String nike) {
        this.nike = nike;
    }
}
