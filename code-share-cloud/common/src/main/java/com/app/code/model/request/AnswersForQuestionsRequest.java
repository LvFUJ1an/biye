package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/5/1
 */
@Data
public class AnswersForQuestionsRequest {
    /**
     * 回答内容
     */
    private String content;
    /**
     * 关联的问题ID
     */
    private Integer questionId;
}
