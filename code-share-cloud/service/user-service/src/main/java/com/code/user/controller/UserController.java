package com.code.user.controller;

import com.app.code.common.BaseResponse;
import com.app.code.common.ResultUtils;
import com.app.code.model.po.User;
import com.app.code.model.request.UserLoginRequest;
import com.app.code.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.code.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lfj
 * @createDate 2025/4/24
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpSession httpSession){
        //校验参数
        if (StringUtils.isAnyEmpty(userLoginRequest.getUsername(),userLoginRequest.getPassword())) {
            throw new RuntimeException("账号密码不能为空");
        }
        UserVO userVO = userService.loginUser(userLoginRequest.getUsername(), userLoginRequest.getPassword());
        httpSession.setAttribute("currentUser", userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestParam("username") String username,
                                          @RequestParam("password") String password,
                                          @RequestParam("email") String email,
                                          @RequestParam("avatarFile") MultipartFile avatarFile,
                                          @RequestParam("role") Integer role){
        Boolean flag = userService.register(username, password, email, avatarFile, role);
        return ResultUtils.success(flag);
    }

    @GetMapping("/getUserById")
    public BaseResponse<User> getUserById(@RequestParam("userId") Integer userId){
        User user = userService.getById(userId);
        return ResultUtils.success(user);
    }

    @PostMapping("/getUserByListId")
    public BaseResponse<List<User>> getUserByListId(@RequestBody List<Integer> userIdList){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId,userIdList);
        List<User> userList = userService.list(queryWrapper);
        return ResultUtils.success(userList);
    }

    @GetMapping("/count")
    public BaseResponse<Integer> getUserCount(){
        Integer count = userService.count();
        return ResultUtils.success(count);
    }
    
    /**
     * 获取本月每天的新增用户数
     * @return 本月每天的新增用户数，格式为：{"日期1": 数量1, "日期2": 数量2, ...}
     */
    @GetMapping("/countByDayInCurrentMonth")
    public BaseResponse<Map<String, Integer>> getUserCountByDayInCurrentMonth() {
        // 获取当前月的第一天和最后一天
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置为当月第一天
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        
        calendar.add(Calendar.MONTH, 1); // 下个月
        calendar.add(Calendar.SECOND, -1); // 减去1秒，即当月最后一天的最后一秒
        Date endDate = calendar.getTime();
        
        // 查询本月注册的所有用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(User::getCreateTime, startDate, endDate);
        List<User> users = userService.list(queryWrapper);
        
        // 按天统计用户注册数量
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Integer> result = new HashMap<>();
        
        // 初始化本月每一天的数据（确保没有用户注册的日期也有记录）
        calendar.setTime(startDate);
        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            result.put(dateFormat.format(calendar.getTime()), 0);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // 统计用户注册数量
        for (User user : users) {
            if (user.getCreateTime() != null) {
                String dateStr = dateFormat.format(user.getCreateTime());
                result.put(dateStr, result.getOrDefault(dateStr, 0) + 1);
            }
        }
        
        return ResultUtils.success(result);
    }
}
