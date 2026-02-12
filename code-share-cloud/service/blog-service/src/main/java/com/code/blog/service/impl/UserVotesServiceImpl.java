package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.UserVotes;
import com.code.blog.service.UserVotesService;
import com.code.blog.mapper.UserVotesMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【user_votes(用户投票记录表)】的数据库操作Service实现
* @createDate 2025-05-02 17:22:37
*/
@Service
public class UserVotesServiceImpl extends ServiceImpl<UserVotesMapper, UserVotes>
    implements UserVotesService{

}




