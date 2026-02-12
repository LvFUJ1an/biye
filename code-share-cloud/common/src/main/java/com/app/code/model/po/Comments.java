package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评论表
 * @TableName comments
 */
@TableName(value ="comments")
@Data
public class Comments {
    /**
     * 评论唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论者ID
     */
    private Integer userId;

    /**
     * 父评论ID
     */
    private Integer parentId;

    /**
     * 关联目标ID
     */
    private Integer targetId;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 评论时间
     */
    private Date createTime;
}