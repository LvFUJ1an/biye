package com.code.blog.service.impl;

import com.app.code.model.po.User;
import com.app.code.model.request.VideoQueryRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Videos;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.VideosService;
import com.code.blog.mapper.VideosMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
* @author lfj
* @description 针对表【videos(视频资源表)】的数据库操作Service实现
* @createDate 2025-04-29 21:45:12
*/
@Service
public class VideosServiceImpl extends ServiceImpl<VideosMapper, Videos>
    implements VideosService{

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public Page<Videos> pageQuery(VideoQueryRequest videoQueryRequest, User user) {
        LambdaQueryWrapper<Videos> wrapper = new LambdaQueryWrapper<>();
        if (user == null){
            wrapper.eq(Videos::getIsPublished, 0);
        }
        wrapper.like(StringUtils.hasText(videoQueryRequest.getKeyword()), Videos::getTitle, videoQueryRequest.getKeyword());
        wrapper.orderByDesc(Videos::getCreateTime);
        // 创建分页对象
        Page<Videos> page = new Page<>(videoQueryRequest.getCurrent(), videoQueryRequest.getSize());
        Page<Videos> list = page(page, wrapper);
        list.getRecords().forEach(item -> {
            item.setUserName(userFeignClient.getUserById(item.getUserId()).getData().getUsername());
            item.setAvatarUrl(userFeignClient.getUserById(item.getUserId()).getData().getAvatarUrl());
        });
        return list;
    }
}




