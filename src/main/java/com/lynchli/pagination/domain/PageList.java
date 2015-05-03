package com.lynchli.pagination.domain;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Lynch
 * @since 2015-04-24
 */
public class PageList<E> extends ArrayList<E> implements Page{


    private int page;
    private int size;
    private int totalCount;

    public PageList(){
        this(PageBounds.DEFAULT_PAGE);
    }

    public PageList(int page){
        this(page, PageBounds.DEFAULT_SIZE);
    }

    public PageList(int page, int size){
        this(page, size, 0);
    }

    public PageList(int page, int size, int totalCount){
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
    }

    public PageList(Collection<? extends E> content) {
        super(content);
    }

    public PageList(Collection<? extends E> content,int page) {
        this(content, page, PageBounds.DEFAULT_SIZE);
    }

    public PageList(Collection<? extends E> content,int page, int size) {
        this(content, page, size, 0);
    }

    public PageList(Collection<? extends E> content,int page, int size, int totalCount) {
        super(content);
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public int getTotalPages() {
        return size == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) size);
    }

    @Override
    public int getStartRow() {
        return page > 0 ? (page - 1) * size + 1 : 0;
    }

    @Override
    public int getEndRow() {
        return page > 0 ? Math.min(size * page, getTotalCount()) : 0;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
