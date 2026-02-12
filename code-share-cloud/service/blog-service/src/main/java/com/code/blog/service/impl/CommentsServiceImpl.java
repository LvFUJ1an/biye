package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.Comments;
import com.code.blog.service.CommentsService;
import com.code.blog.mapper.CommentsMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【comments(评论表)】的数据库操作Service实现
* @createDate 2025-04-27 14:30:57
*/
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments>
    implements CommentsService{

}




