package com.lynchli.pagination.domain;

/**
 * @author Lynch
 * @since 2015-04-24
 */
public interface Page {

    int getPage();

    int getSize();

    int getTotalCount();

    int getTotalPages();

    int getStartRow();

    int getEndRow();
}
