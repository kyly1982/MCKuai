package com.mckuai.imc.Bean;

/**
 * Created by kyly on 2015/11/26.
 */
public class Token {
    private int type;
    private long birthday = 0;
    private long expires = 7200;
    private String token;

    public Token() {
    }

    public Token(int type) {
        this.type = type;
    }

    public Token(int type, long birthday, long expires, String token) {
        this.birthday = birthday;
        this.expires = expires;
        this.token = token;
        this.type = type;
    }

    public void clone(Token token) {
        this.type = token.getType();
        this.birthday = token.getBirthday();
        this.expires = token.getExpires();
        this.token = token.getToken();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getBirthday() {
        return birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTokenSurvival() {
        return (System.currentTimeMillis() - birthday) < expires * 1000;
    }
}
