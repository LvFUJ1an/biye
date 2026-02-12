package com.app.code.model.request;

import lombok.Data;

import java.util.Date;

/**
 * @author lfj
 * @createDate 2025/5/4
 */
@Data
public class QuestionsModifyRequest {
    private Integer id;
    private String title;
    private String content;
}
