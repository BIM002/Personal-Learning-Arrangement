package com.learing.collection.intercept;

import lombok.Data;

import java.util.List;

/**
 * @author: 10302
 * @Date: 2019/12/13 10:51
 * @Description:
 **/
@Data
public class PageBean<T> {

    private int currentPage;
    private int pageSize;
    private int pageCount;
    private int totalCount;

    private List<T> data;

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.setPageCount();
    }

    public void setPageCount() {
        this.pageCount = (int) Math.ceil((double) totalCount / this.pageSize);
    }
}
