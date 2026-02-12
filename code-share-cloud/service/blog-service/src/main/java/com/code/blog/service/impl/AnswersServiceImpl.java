package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Answers;
import com.code.blog.service.AnswersService;
import com.code.blog.mapper.AnswersMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【answers(问题回答表)】的数据库操作Service实现
* @createDate 2025-05-01 17:50:47
*/
@Service
public class AnswersServiceImpl extends ServiceImpl<AnswersMapper, Answers>
    implements AnswersService{

}




