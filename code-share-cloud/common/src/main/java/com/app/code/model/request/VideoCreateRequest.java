package com.app.code.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
@Data
public class VideoCreateRequest {

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
    private MultipartFile videoFile;

    /**
     * 封面图路径
     */
    private MultipartFile image;

    /**
     * 上传用户ID
     */
    private Integer userId;

}
