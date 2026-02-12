package com.code.blog.service.impl;

import com.app.code.model.po.*;
import com.app.code.model.request.VatesQueryRequest;
import com.app.code.model.vo.VotesVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.blog.service.UserVotesService;
import com.code.blog.service.VoteOptionsService;
import com.code.blog.service.VotesService;
import com.code.blog.mapper.VotesMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
* @author lfj
* @description 针对表【votes(投票信息表)】的数据库操作Service实现
* @createDate 2025-05-02 17:22:20
*/
@Service
public class VotesServiceImpl extends ServiceImpl<VotesMapper, Votes>
    implements VotesService{

    @Resource
    private VoteOptionsService voteOptionsService;

    @Resource
    private UserVotesService userVotesService;

    @Override
    public Page<Votes> pageQuery(VatesQueryRequest vatesQueryRequest, User user) {
        LambdaQueryWrapper<Votes> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(vatesQueryRequest.getKeyword()), Votes::getTitle, vatesQueryRequest.getKeyword());
        wrapper.orderByDesc(Votes::getCreateTime);
        // 创建分页对象
        Page<Votes> page = new Page<>(vatesQueryRequest.getCurrent(), vatesQueryRequest.getSize());
        Page<Votes> list = page(page, wrapper);
        list.getRecords().forEach(item -> {
            LambdaQueryWrapper<VoteOptions> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VoteOptions::getVoteId, item.getId());
            List<VoteOptions> optionsList = voteOptionsService.list(queryWrapper);
            item.setOptions(optionsList);
            item.setTotalVotes(0);

            optionsList.forEach(options ->{
                LambdaQueryWrapper<UserVotes> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(UserVotes::getVoteId, item.getId());
                lambdaQueryWrapper.eq(UserVotes::getOptionId, options.getId());
                List<UserVotes> userVotesList = userVotesService.list(lambdaQueryWrapper);
                options.setVoteCount(userVotesList.size());
                item.setTotalVotes(item.getTotalVotes() + options.getVoteCount());
            });
        });
        return list;
    }
}




