package com.lynchli.pagination.domain;

import org.apache.ibatis.session.RowBounds;

import java.util.Optional;

/**
 * @author Lynch
 * @since 2015-04-24
 */
public class PageBounds extends RowBounds {

    public final static int DEFAULT_SIZE = 20;
    public final static int DEFAULT_PAGE = 1;

    private int page;
    private int size;
    private Sort sort;
    private boolean containsTotalCount;
    private boolean asyncTotalCount;

    public PageBounds(){
        this(DEFAULT_PAGE);
    }

    public PageBounds(int page){
        this(page, DEFAULT_SIZE);
    }

    public PageBounds(int page, int size){
        this(page, size, false, false, null);
    }

    public PageBounds(int page, int size, boolean containsTotalCount){
        this(page, size, containsTotalCount, false, null);
    }

    public PageBounds(int page, int size, boolean containsTotalCount, boolean asyncTotalCount){
        this(page, size, containsTotalCount, asyncTotalCount, null);
    }

    public PageBounds(int page, int size, boolean containsTotalCount, boolean asyncTotalCount, Sort.Direction direction, String... properties){
        this(page, size, containsTotalCount, asyncTotalCount, new Sort(direction, properties));
    }

    public PageBounds(int page, int size, boolean containsTotalCount, boolean asyncTotalCount, Sort sort){
        this.page = page <= 0 ? DEFAULT_PAGE : page;
        this.size = size <= 0 ? DEFAULT_PAGE : size;
        this.containsTotalCount = containsTotalCount;
        this.asyncTotalCount = asyncTotalCount;
        this.sort = sort;
    }

    @Override
    public int getOffset() {
        return (page - 1) * size;
    }

    @Override
    public int getLimit() {
        return this.size;
    }

    public PageBounds withPageNumber(int page){
        this.page = page;
        return this;
    }

    public PageBounds withPageSize(int size){
        this.size = size;
        return this;
    }

    public PageBounds withSort(String... properties){
        Optional<Sort> optional = Optional.ofNullable(this.sort);
        this.sort = optional.orElseGet(() -> new Sort(properties));
        sort.withOrder(properties);
        return this;
    }

    public PageBounds withSort(Sort.Direction direction, String... properties){
        Optional<Sort> optional = Optional.ofNullable(this.sort);
        this.sort = optional.orElseGet(() -> new Sort(direction, properties));
        sort.withOrder(direction, properties);
        return this;
    }

    public PageBounds withSort(Sort sort){
        this.sort = sort;
        return this;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public Sort getSort() {
        return sort;
    }

    public boolean getContainsTotalCount() {
        return containsTotalCount;
    }

    public boolean getAsyncTotalCount() {
        return asyncTotalCount;
    }
}
