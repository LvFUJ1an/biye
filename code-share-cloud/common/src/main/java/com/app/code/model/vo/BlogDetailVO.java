package com.app.code.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 博客详情VO，包含评论层级结构
 * @author lfj
 * @createDate 2025/4/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlogDetailVO extends BlogVO {
    /**
     * 顶级评论列表（parentId为0的评论）
     */
    private List<CommentVO> comments = new ArrayList<>();
}
