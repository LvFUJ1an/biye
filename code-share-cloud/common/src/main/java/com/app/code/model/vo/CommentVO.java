package com.app.code.model.vo;

import com.app.code.model.po.Comments;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论VO，包含层级结构
 * @author lfj
 * @createDate 2025/4/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentVO extends Comments {
    /**
     * 评论者用户名
     */
    private String username;
    
    /**
     * 评论者头像URL
     */
    private String avatarUrl;
    
    /**
     * 子评论列表
     */
    private List<CommentVO> children = new ArrayList<>();
} 