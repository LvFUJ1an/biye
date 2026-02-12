package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/28
 */
@Data
public class UserQueryRequest {
    private String keyword;
    private Integer pageSize;
    private Integer currentPage;
}
