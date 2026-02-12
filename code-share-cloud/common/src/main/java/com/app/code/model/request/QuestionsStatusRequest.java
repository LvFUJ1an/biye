package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/5/1
 */
@Data
public class QuestionsStatusRequest {
    private Integer questionId;
    private Integer isResolved;
}
