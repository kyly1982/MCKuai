package com.mckuai.imc.Bean;

import java.util.ArrayList;

/**
 * Created by kyly on 2017/6/21.
 */

public class RecommendsBean {
    private int allCount;
    private int page;
    private int pageCount;
    private int pageSize;
    private ArrayList<RecommendItem> data;

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

    public ArrayList<RecommendItem> getData() {
        return data;
    }

    public void setData(ArrayList<RecommendItem> data) {
        this.data = data;
    }
}
