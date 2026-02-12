package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 投票选项表
 * @TableName vote_options
 */
@TableName(value ="vote_options")
@Data
public class VoteOptions {
    /**
     * 选项唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的投票ID
     */
    private Integer voteId;

    /**
     * 选项内容
     */
    private String optionText;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 投票数量
     */
    @TableField(exist = false)
    private Integer voteCount;
}