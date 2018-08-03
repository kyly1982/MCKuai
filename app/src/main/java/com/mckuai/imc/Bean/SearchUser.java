package com.mckuai.imc.Bean;

import java.util.ArrayList;

/**
 * Created by kyly on 2016/7/22.
 */
public class SearchUser {
    private int allCount = 0;
    private ArrayList<MCUser> data;
    private int page = 1;
    private int pageCount = 0;
    private int pageSize = 20;

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public ArrayList<MCUser> getData() {
        return data;
    }

    public void setData(ArrayList<MCUser> data) {
        this.data = data;
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
