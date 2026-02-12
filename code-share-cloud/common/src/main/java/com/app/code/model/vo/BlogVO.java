package com.app.code.model.vo;

import com.app.code.model.po.Blogs;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lfj
 * @createDate 2025/4/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlogVO extends Blogs {
    private String authorName;
    private Integer likesCount;
    private Integer commentCount;
    private String sectionName;
    private String authorAvatar;
    private String isLiked;
    private Boolean isFollow;

}
