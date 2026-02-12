package com.code.blog.controller;

import com.alibaba.fastjson.JSONObject;
import com.app.code.common.BaseResponse;
import com.app.code.model.po.Follow;
import com.app.code.model.po.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.code.blog.service.FollowService;
import com.code.blog.ws.WebSocketServer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author lfj
 * @createDate 2025/5/2
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    /**
     * 关注和取关用户
     */
    @PostMapping("/{id}")
    public BaseResponse<Integer> likeBlog(@PathVariable Integer id ,HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        if  (userInfo == null) {
            return BaseResponse.error(401, "请先登录");
        }
        User user = JSONObject.parseObject(userInfo, User.class);
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, id);
        Follow follow = followService.getOne(queryWrapper);
        if (follow == null) {
            follow = new Follow();
            follow.setUserId(id);
            List<Integer> list = Arrays.asList(user.getId());
            follow.setFollowIdList(JSONObject.toJSONString(list));
            boolean flag = followService.save(follow);
            if (!flag){
                return BaseResponse.error(500, "关注失败");
            }
            WebSocketServer.OnlineUser onlineUser = new WebSocketServer.OnlineUser(
                    user.getId(),
                    user.getUsername(),
                    user.getAvatarUrl()
            );
            WebSocketServer.sendFollowNotification(id, onlineUser);
            return BaseResponse.success(0);
        }
        List<Integer> followIdList = JSONObject.parseArray(follow.getFollowIdList(), Integer.class);
        if (followIdList.contains(user.getId())) {
            followIdList.remove(user.getId());
            follow.setFollowIdList(JSONObject.toJSONString(followIdList));
            boolean flag = followService.updateById(follow);
            if (!flag) {
                return BaseResponse.error(500, "取关失败");
            }
            return BaseResponse.success(1);
        }
        followIdList.add(user.getId());
        follow.setFollowIdList(JSONObject.toJSONString(followIdList));
        boolean flag = followService.updateById(follow);
        if (!flag) {
            return BaseResponse.error(500, "关注失败");
        }
        WebSocketServer.OnlineUser onlineUser = new WebSocketServer.OnlineUser(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl()
        );
        WebSocketServer.sendFollowNotification(id, onlineUser);
        return BaseResponse.success(0);
    }
}
