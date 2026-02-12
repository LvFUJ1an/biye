package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/5/1
 */
@Data
public class QuestionsRequest {
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
