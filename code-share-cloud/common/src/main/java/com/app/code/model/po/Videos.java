package com.app.code.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 视频资源表
 * @TableName videos
 */
@TableName(value ="videos")
@Data
public class Videos {
    /**
     * 视频唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 视频存储路径
     */
    private String videoPath;

    /**
     * 封面图路径
     */
    private String coverPath;

    /**
     * 上传用户ID
     */
    private Integer userId;

    /**
     * 上架状态：0-下架，1-上架
     */
    private Integer isPublished;

    /**
     * 上传时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;


    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String avatarUrl;

}