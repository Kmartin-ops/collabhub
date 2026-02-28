package com.collabhub.report;

import com.collabhub.domain.BaseEntity;

import java.util.List;

public class PagedResult<T extends BaseEntity> {

    private final List<T> content;   // the items on this page
    private final int page;          // current page number (0-based)
    private final int pageSize;      // items per page
    private final long totalElements; // total items across all pages

    public PagedResult(List<T> content, int page, int pageSize, long totalElements) {
        this.content = content;
        this.page = page;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    // Derived values
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public boolean hasNext() {
        return page < getTotalPages() - 1;
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    // Getters
    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }

    @Override
    public String toString() {
        return "PagedResult{page=" + page + "/" + (getTotalPages() - 1) +
                ", size=" + content.size() +
                ", total=" + totalElements + "}";
    }
}