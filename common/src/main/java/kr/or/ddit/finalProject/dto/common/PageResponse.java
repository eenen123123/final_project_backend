package kr.or.ddit.finalProject.dto.common;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResponse<T> {
    private List<T> items;
    private int totalCount;
    private int totalPages;

    public PageResponse(List<T> items, int totalCount) {
        this.items = items;
        this.totalCount = totalCount;
        this.totalPages = 0;
    }

    public PageResponse(List<T> items, int totalCount, int totalPages) {
        this.items = items;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
    }
}
