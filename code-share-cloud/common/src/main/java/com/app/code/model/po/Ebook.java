package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName ebook
 */
@TableName(value ="ebook")
@Data
public class Ebook {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private String image;

    /**
     * 
     */
    private String filePath;

    /**
     * 
     */
    private Integer isPublished;

    /**
     * 
     */
    private Date createTime;

    private Integer viewCount;

    private Date updateTime;
}