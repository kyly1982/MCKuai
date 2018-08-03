package com.mckuai.imc.Bean;

import java.util.ArrayList;

public class CommunityDynamicList {
    private ArrayList<CommunityDynamic> data;
    private int allCount;
    private int page;
    private int pageCount;
    private int pageSize;

    public ArrayList<CommunityDynamic> getData() {
        return data;
    }

    public void setData(ArrayList<CommunityDynamic> data) {
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

    public int getNextPage() {
        return page + 1;
    }

    public int getPageCount() {
        pageCount = allCount / pageSize + (0 == allCount % pageSize ? 0 : 1);
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

    public boolean EOF() {
        if (0 == allCount) {
            return true;
        }
        return page == getPageCount();
    }

    public void resetPage() {
        this.page = 0;
    }
}
