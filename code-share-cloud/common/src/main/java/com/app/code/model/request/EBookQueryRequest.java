package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
@Data
public class EBookQueryRequest {
    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 关键词
     */
    private String keyword;
}
