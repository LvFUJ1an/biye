package com.code.blog.service;

import com.app.code.model.po.User;
import com.app.code.model.po.Videos;
import com.app.code.model.request.VideoQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfj
* @description 针对表【videos(视频资源表)】的数据库操作Service
* @createDate 2025-04-29 21:45:12
*/
public interface VideosService extends IService<Videos> {

    Page<Videos> pageQuery(VideoQueryRequest videoQueryRequest, User user);
}
