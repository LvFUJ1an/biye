package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Blogs;
import com.code.blog.service.BlogsService;
import com.code.blog.mapper.BlogsMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【blogs(技术博客表)】的数据库操作Service实现
* @createDate 2025-04-27 09:36:34
*/
@Service
public class BlogsServiceImpl extends ServiceImpl<BlogsMapper, Blogs>
    implements BlogsService{

}




