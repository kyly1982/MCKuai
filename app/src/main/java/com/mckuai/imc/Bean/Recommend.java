package com.mckuai.imc.Bean;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/20.
 */
public class Recommend {
    private ArrayList<Post> live = new ArrayList<Post>(3);
    private ArrayList<Post> talk = new ArrayList<Post>(10);


    public ArrayList<Post> getLive() {
        return live;
    }

    public void setLive(ArrayList<Post> live) {
        this.live = live;
    }

    public ArrayList<Post> getTalk() {
        return talk;
    }

    public void setTalk(ArrayList<Post> talk) {
        this.talk = talk;
    }

    public ArrayList<Post> getAllPostList() {
        ArrayList<Post> temp = (ArrayList<Post>) live.clone();
        temp.addAll(talk);
        return temp;
    }
}
