package com.mckuai.imc.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class VideoBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<Post> data;//直播帖子
    private int page;//当前页数
    private int pageSize;//分页大小
    private int allCount;//总页数


    public ArrayList<Post> getData() {
        return data;
    }

    public void setData(ArrayList<Post> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public Page getPageInfo() {
        Page videoPage = new Page(allCount, page, pageSize);
        return videoPage;
    }
}
