package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * 投票信息表
 * @TableName votes
 */
@TableName(value ="votes")
@Data
public class Votes {
    /**
     * 投票唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 投票标题
     */
    private String title;

    /**
     * 发起用户ID
     */
    private Integer userId;

    /**
     * 投票类型：0-单选，1-多选
     */
    private Integer voteType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 截止时间
     */
    private Date endTime;

    /**
     * 投票选项
     */
    @TableField(exist = false)
    private List<VoteOptions> options;

    /**
     * 投票总数
     */
    @TableField(exist = false)
    private Integer totalVotes;

    /**
     * 是否已投票
     */
    @TableField(exist = false)
    private Boolean hasVoted;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String avatarUrl;

}