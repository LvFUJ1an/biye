package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 用户投票记录表
 * @TableName user_votes
 */
@TableName(value ="user_votes")
@Data
public class UserVotes {
    /**
     * 记录唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 投票用户ID
     */
    private Integer userId;

    /**
     * 关联的投票ID
     */
    private Integer voteId;

    /**
     * 选择的选项ID
     */
    private Integer optionId;

    /**
     * 投票时间
     */
    private Date createTime;
}