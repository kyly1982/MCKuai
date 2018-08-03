package com.mckuai.imc.Bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by kyly on 2015/10/12.
 */
public class Page implements Serializable {
    private int page = 0;
    private int allCount = 0;
    private int pageSize = 20;
    private int pageCount;

    public Page(int allCount, int page, int pageSize) {
        this.allCount = allCount;
        this.page = page;
        this.pageSize = pageSize;
        this.pageCount = (0 == (allCount % pageSize) ? (allCount / pageSize) : ((allCount / pageSize) + 1));
    }

    public Page() {
        this.page = 0;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int count) {
        this.allCount = count;
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

        pageCount = (0 == (allCount % pageSize) ? (allCount / pageSize) : ((allCount / pageSize) + 1));
        return page == pageCount;
    }

    public int getNextPage() {
        if (0 == page || 0 == allCount || allCount < pageSize) {
            return 1;
        }
        int pagecount = (0 == (allCount % pageSize) ? (allCount / pageSize) : ((allCount / pageSize) + 1));
        if (page < pagecount) {
            return page + 1;
        } else return page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public Page clone(@NonNull Page page) {
        this.page = page.getPage();
        this.pageSize = page.getPageSize();
        this.allCount = page.getAllCount();
        return this;
    }
}
