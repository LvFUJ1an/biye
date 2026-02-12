package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 技术问题提问表
 * @TableName questions
 */
@TableName(value ="questions")
@Data
public class Questions {
    /**
     * 提问唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 提问标题
     */
    private String title;

    /**
     * 提问内容
     */
    private String content;

    /**
     * 提问者ID
     */
    private Integer userId;

    /**
     * 是否已解决：0-未解决，1-已解决
     */
    private Integer isResolved;

    /**
     * 提问时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 发布者用户名
     */
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String avatarUrl;
}