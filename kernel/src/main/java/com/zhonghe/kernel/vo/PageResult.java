package com.zhonghe.kernel.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Paginated result wrapper
 * @param <T> Type of the items in the list
 */
@Getter
@Setter
public class PageResult<T> {
    private List<T> list;      // Current page data
    private long total;        // Total number of items
    private int page;          // Current page number
    private int pageSize;      // Items per page

    // Empty constructor for frameworks
    public PageResult() {}

    // Convenience constructor
    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * Calculate total pages
     * @return Total page count
     */
    public int getTotalPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

    /**
     * Check if there's a next page
     * @return true if there's more data
     */
    public boolean hasNext() {
        return page < getTotalPages();
    }

    /**
     * Check if there's a previous page
     * @return true if not on first page
     */
    public boolean hasPrevious() {
        return page > 1;
    }
}