package com.code.blog.service;

import com.app.code.model.po.Questions;
import com.app.code.model.po.User;
import com.app.code.model.request.QuestionsRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lfj
* @description 针对表【questions(技术问题提问表)】的数据库操作Service
* @createDate 2025-05-01 17:57:26
*/
public interface QuestionsService extends IService<Questions> {

    Page<Questions> pageQuery(QuestionsRequest questionRequest, User user);
}
