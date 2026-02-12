package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/5/3
 */
@Data
public class ModifyBlogRequest {
    private Integer id;

    private String title;

    private String content;

    private Integer sectionId;
    //'状态：0-草稿，1-已发布'
    private Integer status;
}
