package com.code.blog.controller;

import com.app.code.common.BaseResponse;
import com.app.code.common.ResultUtils;
import com.app.code.model.po.*;
import com.app.code.model.request.UserAddVotesRequest;
import com.app.code.model.request.VatesQueryRequest;
import com.app.code.model.request.VideoQueryRequest;
import com.app.code.model.request.VotesRequest;
import com.app.code.model.vo.VotesVO;
import com.app.code.util.UserUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.UserVotesService;
import com.code.blog.service.VoteOptionsService;
import com.code.blog.service.VotesService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@RestController
@RequestMapping("/votes")
public class VotesController {

    @Resource
    private VotesService votesService;
    @Resource
    private VoteOptionsService voteOptionsService;
    @Resource
    private UserVotesService userVotesService;
    @Resource
    private UserFeignClient userFeignClient;;

    /**
     * 新增投票
     */
    @PostMapping("/create")
    public BaseResponse<Boolean> create(@RequestBody VotesRequest voteRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        if (user == null){
            return ResultUtils.error(40101, "未登录");
        }
        String title = voteRequest.getTitle();
        Integer voteType = voteRequest.getVoteType();
        if (StringUtils.isBlank(title) || voteType == null){
            return ResultUtils.error(40001, "参数错误");
        }
        Votes votes = new Votes();
        votes.setTitle(title);
        votes.setVoteType(voteType);
        votes.setUserId(user.getId());
        votes.setCreateTime(new java.util.Date());
        votes.setEndTime(voteRequest.getEndTime());
        boolean flag = votesService.save(votes);
        if (!flag){
            return ResultUtils.error(500, "创建投票失败");
        }

        //保存选项
        List<String> optionTextList = voteRequest.getOptionTextList();
        ArrayList<VoteOptions> voteOptions = new ArrayList<>();
        for (String optionText : optionTextList) {
            VoteOptions voteOption = new VoteOptions();
            voteOption.setVoteId(votes.getId());
            voteOption.setOptionText(optionText);
            voteOptions.add(voteOption);
        }
        boolean voteOptionsFlag = voteOptionsService.saveBatch(voteOptions);
        if (!voteOptionsFlag){
            return ResultUtils.error(500, "创建投票选项失败");
        }
        return ResultUtils.success(true);
    }


    @PostMapping("/page")
    public BaseResponse<Page<Votes>> pageQuery(@RequestBody VatesQueryRequest vatesQueryRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        Page<Votes> page = votesService.pageQuery(vatesQueryRequest,user);
        return BaseResponse.success(page);
    }


    @PostMapping("/getById/{id}")
    public BaseResponse<Votes> getVideoById(@PathVariable("id") Integer id,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        Votes item = votesService.getById(id);
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
            userVotesList.forEach(userVotes ->{
                if (user !=null){
                    if (userVotes.getUserId().equals(user.getId())){
                        item.setHasVoted(true);
                    }
                }
            });
        });
        BaseResponse<User> response = userFeignClient.getUserById(item.getUserId());
        if (response == null || response.getData() == null){
            return BaseResponse.error(400, "用户不存在");
        }
        item.setUserName(response.getData().getUsername());
        item.setAvatarUrl(response.getData().getAvatarUrl());
        return BaseResponse.success(item);
    }

    /**
     * 投票
     */
    @PostMapping("/vote")
    public BaseResponse<Boolean> vote(@RequestBody UserAddVotesRequest userAddVotesRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        if (user == null){
            return ResultUtils.error(40101, "未登录");
        }
        List<Integer> optionIdList = userAddVotesRequest.getOptionIdList();
        if (optionIdList == null || optionIdList.isEmpty()){
            return ResultUtils.error(40001, "参数错误");
        }
        ArrayList<UserVotes> userVotesArrayList = new ArrayList<>();
        for (Integer optionId : optionIdList) {
            UserVotes userVotes = new UserVotes();
            userVotes.setUserId(user.getId());
            userVotes.setVoteId(userAddVotesRequest.getVoteId());
            userVotes.setOptionId(optionId);
            userVotes.setCreateTime(new java.util.Date());
            userVotesArrayList.add(userVotes);
        }
        boolean flag = userVotesService.saveBatch(userVotesArrayList);
        if (!flag){
            return ResultUtils.error(500, "投票失败");
        }
        return ResultUtils.success(true);
    }

    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteVideo(@PathVariable("id") Integer id,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        if (user == null){
            return ResultUtils.error(40101, "未登录");
        }
        Votes votes = votesService.getById(id);
        if (votes == null){
            return ResultUtils.error(40001, "参数错误");
        }
        if (!votes.getUserId().equals(user.getId())){
            return ResultUtils.error(403, "无权操作");
        }
        boolean result = votesService.removeById(id);
        if (!result){
            return ResultUtils.error(500, "删除失败");
        }
        LambdaQueryWrapper<UserVotes> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVotes::getVoteId, id);
        boolean flag = userVotesService.remove(queryWrapper);
        if (!flag){
            return ResultUtils.error(500, "删除失败");
        }
        return ResultUtils.success(true);
    }
}
