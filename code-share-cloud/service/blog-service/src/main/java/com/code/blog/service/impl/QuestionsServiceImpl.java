package com.code.blog.service.impl;

import com.app.code.model.po.Ebook;
import com.app.code.model.po.User;
import com.app.code.model.request.QuestionsRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Questions;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.QuestionsService;
import com.code.blog.mapper.QuestionsMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
* @author lfj
* @description 针对表【questions(技术问题提问表)】的数据库操作Service实现
* @createDate 2025-05-01 17:57:26
*/
@Service
public class QuestionsServiceImpl extends ServiceImpl<QuestionsMapper, Questions>
    implements QuestionsService{

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public Page<Questions> pageQuery(QuestionsRequest questionRequest, User user) {
        LambdaQueryWrapper<Questions> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(questionRequest.getKeyword()), Questions::getTitle, questionRequest.getKeyword());
        wrapper.orderByDesc(Questions::getCreateTime);
        // 创建分页对象
        Page<Questions> page = new Page<>(questionRequest.getCurrent(), questionRequest.getSize());
        Page<Questions> list = page(page, wrapper);
        list.getRecords().forEach(item -> {
            item.setUserName(userFeignClient.getUserById(item.getUserId()).getData().getUsername());
            item.setAvatarUrl(userFeignClient.getUserById(item.getUserId()).getData().getAvatarUrl());
        });
        return list;
    }
}




