package com.app.code.model.dto;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/27
 */
@Data
public class BlogDTO {
    private String title;

    private String content;

    private Integer authorId;

    private Integer sectionId;
    //'状态：0-草稿，1-已发布'
    private Integer status;
    //当前版本号
    private Integer version;
}
