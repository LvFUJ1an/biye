package com.code.user.controller;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.User;
import com.app.code.model.request.UserQueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 管理员用户管理接口
 * @author lfj
 * @createDate 2025/4/27
 */
@RestController
@RequestMapping("/admin")
public class AdminUserController {
    
    @Resource
    private UserService userService;
    
    /**
     * 管理员删除用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/user/{userId}")
    public BaseResponse<Boolean> deleteUser(@PathVariable String userId) {
        Boolean result = userService.deleteUser(userId);
        return BaseResponse.success(result);
    }
    
    /**
     * 管理员禁用用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/user/disable/{userId}")
    public BaseResponse<Boolean> disableUser(@PathVariable String userId) {
        Boolean result = userService.updateUserStatus(userId, 1);
        return BaseResponse.success(result);
    }
    
    /**
     * 管理员启用用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/user/enable/{userId}")
    public BaseResponse<Boolean> enableUser(@PathVariable String userId) {
        Boolean result = userService.updateUserStatus(userId, 0);
        return BaseResponse.success(result);
    }
    
    /**
     * 管理员重置用户密码
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/user/resetPassword/{userId}")
    public BaseResponse<Boolean> resetUserPassword(@PathVariable String userId) {
        Boolean result = userService.resetUserPassword(userId);
        return BaseResponse.success(result);
    }

    /**
     * 管理员获取用户列表
     */
    @PostMapping("/user/list")
    public BaseResponse<IPage<User>> listUsersByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return BaseResponse.success(userService.getUserList(userQueryRequest));
    }






}
