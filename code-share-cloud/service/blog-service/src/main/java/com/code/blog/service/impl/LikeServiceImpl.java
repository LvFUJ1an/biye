package com.code.blog.service.impl;

import com.app.code.model.po.Likes;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.blog.service.LikeService;
import com.code.blog.mapper.LikeMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【like】的数据库操作Service实现
* @createDate 2025-05-01 21:39:42
*/
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Likes>
    implements LikeService{

}




