package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName blogs
 */
@TableName(value ="blogs")
@Data
public class Blogs {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String dataId;

    private String title;

    private String content;

    private Integer authorId;

    private Integer sectionId;
    //'状态：0-草稿，1-已发布'
    private Integer status;
    //当前版本号
    private Integer version;

    private Integer likesCount;

    private Date createTime;

    private Date updateTime;

    private Integer viewCount;


}