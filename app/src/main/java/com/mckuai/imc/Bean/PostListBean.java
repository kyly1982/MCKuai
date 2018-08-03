package com.mckuai.imc.Bean;

import java.io.Serializable;
import java.util.ArrayList;

public class PostListBean implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8232839695607120404L;
    private int allCount = 0;
    private ArrayList<Post> data;
    private int page = 0;
    private int pageCount = 0;
    private int pageSize = 20;

    public ArrayList<Post> getdata() {
        return data;
    }

    public void setdata(ArrayList<Post> data) {
        this.data = data;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        if (0 != pageSize) {
            pageCount = allCount / pageSize + (0 == allCount % pageSize ? 0 : 1);
        }
        this.allCount = allCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
        if (0 != pageSize) {
            pageCount = allCount / pageSize + (0 == allCount % pageSize ? 0 : 1);
        }

    }

    public int getNextPage() {
        return page + 1;
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
        if (0 != pageSize) {
            pageCount = allCount / pageSize + (0 == allCount % pageSize ? 0 : 1);
        }
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
