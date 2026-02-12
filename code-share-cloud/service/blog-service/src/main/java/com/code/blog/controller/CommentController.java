package com.code.blog.controller;

import com.alibaba.fastjson.JSONObject;
import com.app.code.common.BaseResponse;
import com.app.code.model.dto.CommentCreateDTO;
import com.app.code.model.po.Blogs;
import com.app.code.model.po.Comments;
import com.app.code.model.po.User;
import com.app.code.model.vo.CommentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.BlogsService;
import com.code.blog.service.CommentsService;
import com.code.blog.ws.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 评论管理接口
 * @author lfj
 * @createDate 2025/4/27
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    
    @Resource
    private CommentsService commentsService;
    
    @Resource
    private BlogsService blogsService;
    
    @Resource
    private UserFeignClient userFeignClient;
    
    /**
     * 发表评论
     * @param commentCreateDTO 评论信息
     * @param request HTTP请求
     * @return 评论信息
     */
    @PostMapping("/create")
    public BaseResponse<CommentVO> createComment(@RequestBody @Valid CommentCreateDTO commentCreateDTO, 
                                                HttpServletRequest request) {
        // 1. 获取当前登录用户信息
        String userInfo = request.getHeader("X-User-Info");
        if (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        
        // 2. 验证博客是否存在
        Blogs blog = blogsService.getById(commentCreateDTO.getTargetId());
        if (blog == null) {
            return BaseResponse.error(404, "博客不存在");
        }
        
        // 3. 如果是回复评论，验证父评论是否存在
        if (commentCreateDTO.getParentId() != 0) {
            Comments parentComment = commentsService.getById(commentCreateDTO.getParentId());
            if (parentComment == null) {
                return BaseResponse.error(404, "父评论不存在");
            }
            
            // 确保父评论属于同一篇博客
            if (!parentComment.getTargetId().equals(commentCreateDTO.getTargetId())) {
                return BaseResponse.error(400, "父评论不属于该博客");
            }
        }
        
        // 4. 创建评论对象
        Comments comment = new Comments();
        comment.setContent(commentCreateDTO.getContent());
        comment.setUserId(user.getId());
        comment.setParentId(commentCreateDTO.getParentId());
        comment.setTargetId(commentCreateDTO.getTargetId());
        comment.setLikes(0);
        comment.setCreateTime(new Date());
        
        // 5. 保存评论
        boolean result = commentsService.save(comment);
        if (!result) {
            return BaseResponse.error(500, "评论发表失败");
        }
        
        // 6. 组装返回数据
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        commentVO.setUsername(user.getUsername());
        commentVO.setAvatarUrl(user.getAvatarUrl());

        WebSocketServer.OnlineUser onlineUser = new WebSocketServer.OnlineUser(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl()
        );
        WebSocketServer.sendCommentNotification(
                blog.getAuthorId(),
                onlineUser,
                commentCreateDTO.getContent(),
                "post",
                commentCreateDTO.getTargetId()
        );

        return BaseResponse.success(commentVO);
    }
    
    /**
     * 删除评论
     * @param id 评论ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteComment(@PathVariable Integer id, HttpServletRequest request) {
        // 1. 获取当前登录用户信息
        String userInfo = request.getHeader("X-User-Info");
        if (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        
        // 2. 验证评论是否存在
        Comments comment = commentsService.getById(id);
        if (comment == null) {
            return BaseResponse.error(404, "评论不存在");
        }
        
        // 3. 验证用户是否有权限删除该评论（评论者本人或管理员）
        boolean isAdmin = user.getRole() != null && user.getRole() == 1; // 假设角色1为管理员
        if (!comment.getUserId().equals(user.getId()) && !isAdmin) {
            return BaseResponse.error(403, "无权删除该评论");
        }
        
        // 4. 查找并删除所有子评论（递归删除）
        List<Integer> commentIdsToDelete = new ArrayList<>();
        collectCommentIdsToDelete(id, commentIdsToDelete);
        
        // 5. 删除所有相关评论（包括子评论和评论本身）
        if (!commentIdsToDelete.isEmpty()) {
            commentsService.removeByIds(commentIdsToDelete);
        }
        
        return BaseResponse.success(true);
    }
    
    /**
     * 递归收集需要删除的评论ID
     * @param commentId 评论ID
     * @param commentIdsToDelete 需要删除的评论ID列表
     */
    private void collectCommentIdsToDelete(Integer commentId, List<Integer> commentIdsToDelete) {
        // 添加当前评论ID
        commentIdsToDelete.add(commentId);
        
        // 查找所有子评论
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getParentId, commentId);
        List<Comments> childComments = commentsService.list(queryWrapper);
        
        // 递归处理每个子评论
        for (Comments childComment : childComments) {
            collectCommentIdsToDelete(childComment.getId(), commentIdsToDelete);
        }
    }
}
