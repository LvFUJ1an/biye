package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 问题回答表
 * @TableName answers
 */
@TableName(value ="answers")
@Data
public class Answers {
    /**
     * 回答唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 回答者ID
     */
    private Integer userId;

    /**
     * 关联的问题ID
     */
    private Integer questionId;

    /**
     * 是否被采纳：0-未采纳，1-已采纳
     */
    private Integer isAccepted;

    /**
     * 回答时间
     */
    private Date createTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String avatarUrl;
}