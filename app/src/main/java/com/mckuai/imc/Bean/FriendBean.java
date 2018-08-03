package com.mckuai.imc.Bean;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/2/2.
 */
public class FriendBean {
    private ArrayList<MCUser> data;
    private int allCount;
    private int page;
    private int pageCount;
    private int pageSize;

    public ArrayList<MCUser> getData() {
        return data;
    }

    public void setData(ArrayList<MCUser> data) {
        this.data = data;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
