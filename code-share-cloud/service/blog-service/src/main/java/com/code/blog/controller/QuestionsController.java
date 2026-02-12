package com.code.blog.controller;

import com.alibaba.fastjson.JSONObject;
import com.app.code.common.BaseResponse;
import com.app.code.common.ResultUtils;
import com.app.code.model.po.*;
import com.app.code.model.request.*;
import com.app.code.model.vo.QuestionsVO;
import com.app.code.util.UserUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.AnswersService;
import com.code.blog.service.QuestionsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/1
 */
@RestController
@RequestMapping("/questions")
public class QuestionsController {

    @Resource
    private QuestionsService questionsService;

    @Resource
    private AnswersService answersService;

    @Resource
    private UserFeignClient userFeignClient;

    @PostMapping("/page")
    public BaseResponse<Page<Questions>> pageQuery(@RequestBody QuestionsRequest questionRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        Page<Questions> page = questionsService.pageQuery(questionRequest,user);
        return BaseResponse.success(page);
    }

    @PostMapping("create")
    public BaseResponse<Integer> updateEbook(@RequestBody Questions questions, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        questions.setUserId(user.getId());
        // 设置创建时间和更新时间
        Date now = new Date();
        questions.setCreateTime(now);
        questions.setUpdateTime(now);
        boolean result = questionsService.save(questions);
        if (result) {
            return BaseResponse.success(questions.getId());
        } else {
            return BaseResponse.error(500, "创建博客失败");
        }
    }

    @PostMapping("/modify")
    public BaseResponse<Boolean> createEbook(@RequestBody QuestionsModifyRequest questionModifyRequest) {
        if (questionModifyRequest == null || questionModifyRequest.getId() == null){
            return BaseResponse.error(400, "参数不能为空");
        }
        Questions questions = questionsService.getById(questionModifyRequest.getId());
        if (questions == null){
            return BaseResponse.error(400, "问题不存在");
        }
        if (questionModifyRequest.getTitle() != null){
            questions.setTitle(questionModifyRequest.getTitle());
        }
        if (questionModifyRequest.getContent() != null){
            questions.setContent(questionModifyRequest.getContent());
        }
        questions.setUpdateTime(new Date());
        boolean flag = questionsService.updateById(questions);
        return BaseResponse.success(flag);
    }

    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteEbook(@PathVariable("id") Integer id) {
        boolean delete = questionsService.removeById(id);
        return BaseResponse.success(delete);
    }

    /**
     * 设置问题是否解决
     * @param questionsStatusRequest
     * @return
     */
    @PostMapping("/status")
    public BaseResponse<Boolean> updateEbookStatus(@RequestBody QuestionsStatusRequest questionsStatusRequest) {

        Questions questions = questionsService.getById(questionsStatusRequest.getQuestionId());
        if (questions == null){
            return BaseResponse.error(400, "问题不存在");
        }
        questions.setIsResolved(questionsStatusRequest.getIsResolved());
        return ResultUtils.success(true);
    }

    @PostMapping("/getQuestionById/{id}")
    public BaseResponse<QuestionsVO> getVideoById(@PathVariable("id") Integer id) {
        Questions questions = questionsService.getById(id);
        if (questions == null){
            return BaseResponse.error(400, "问题不存在");
        }
        //获取提问下所有的答案，并查询出答案的状态
        LambdaQueryWrapper<Answers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answers::getQuestionId, id);
        queryWrapper.orderByDesc(Answers::getCreateTime);
        Integer userId = questions.getUserId();
        BaseResponse<User> response = userFeignClient.getUserById(userId);
        if (response == null || response.getData() == null){
            return BaseResponse.error(400, "用户不存在");
        }
        User user = response.getData();
        List<Answers> answersList = answersService.list(queryWrapper);
        answersList.forEach(item -> {
            BaseResponse<User> userBaseResponse = userFeignClient.getUserById(item.getUserId());
            item.setUsername(userBaseResponse.getData().getUsername());
            item.setAvatarUrl(userBaseResponse.getData().getAvatarUrl());
        });

        QuestionsVO questionsVO = new QuestionsVO(questions);
        questionsVO.setAnswers(answersList);
        questionsVO.setUser(user);


        return BaseResponse.success(questionsVO);
    }


    /**
     * 回答问题
     */
    @PostMapping("/answer")
    public BaseResponse<Boolean> answerQuestion(@RequestBody AnswersForQuestionsRequest answerRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        Answers answers = new Answers();
        answers.setContent(answerRequest.getContent());
        answers.setQuestionId(answerRequest.getQuestionId());
        answers.setUserId(user.getId());
        answers.setCreateTime(new Date());
        answers.setIsAccepted(0);
        boolean result = answersService.save(answers);
        if (result) {
            return BaseResponse.success(true);
        } else {
            return BaseResponse.error(500, "回答失败");
        }
    }

    /**
     * 删除回答
     */
    @PostMapping("/deleteAnswers/{id}")
    public BaseResponse<Boolean> deleteAnswers(@PathVariable Integer id,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        Answers answers = answersService.getById(id);
        if (answers == null) {
            return BaseResponse.error(400, "回答不存在");
        }
        if (!answers.getUserId().equals(user.getId())) {
            return BaseResponse.error(403, "无权删除该回答");
        }
        boolean result = answersService.removeById(id);
        return BaseResponse.success(result);
    }

    /**
     * 采纳回答
     */
        @PostMapping("/acceptAnswer/{id}")
    public BaseResponse<Boolean> acceptAnswer(@PathVariable("id") String id, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        Answers answers = answersService.getById(id);
        if (answers == null) {
            return BaseResponse.error(400, "回答不存在");
        }
//        if (!answers.getUserId().equals(user.getId())) {
//            return BaseResponse.error(403, "无权操作该回答");
//        }
        answers.setIsAccepted(1);
        boolean result = answersService.updateById(answers);
        return BaseResponse.success(result);
    }


    @PostMapping("/solve/{id}")
    public BaseResponse<Boolean> adoptAnswer(@PathVariable("id") Integer id, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        Questions questions = questionsService.getById(id);
        if (questions == null) {
            return BaseResponse.error(400, "问题不存在");
        }
        if (!questions.getUserId().equals(user.getId())) {
            return BaseResponse.error(403, "无权操作该问题");
        }
        questions.setIsResolved(1);
        boolean result = questionsService.updateById(questions);
        return BaseResponse.success(result);
    }
}
