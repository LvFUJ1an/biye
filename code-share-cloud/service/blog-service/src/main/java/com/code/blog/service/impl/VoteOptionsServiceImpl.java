package com.code.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.code.model.po.VoteOptions;
import com.code.blog.service.VoteOptionsService;
import com.code.blog.mapper.VoteOptionsMapper;
import org.springframework.stereotype.Service;

/**
* @author lfj
* @description 针对表【vote_options(投票选项表)】的数据库操作Service实现
* @createDate 2025-05-02 17:22:29
*/
@Service
public class VoteOptionsServiceImpl extends ServiceImpl<VoteOptionsMapper, VoteOptions>
    implements VoteOptionsService{

}




