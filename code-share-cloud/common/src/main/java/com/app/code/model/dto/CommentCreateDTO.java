package com.app.code.model.dto;

import lombok.Data;

/**
 * 创建评论的请求DTO
 * @author lfj
 * @createDate 2025/4/27
 */
@Data
public class CommentCreateDTO {
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 关联目标ID（博客ID）
     */
    private Integer targetId;
    
    /**
     * 父评论ID，默认为0表示顶级评论
     */
    private Integer parentId = 0;
} 