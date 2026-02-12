package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
@Data
public class EBookStatusRequest {
    private Integer EBookId;
    private Integer isPublished;
}
