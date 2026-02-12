package com.code.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.app.code.common.BaseResponse;
import com.app.code.exception.BusinessException;
import com.app.code.model.po.User;
import com.app.code.model.request.UserQueryRequest;
import com.app.code.model.request.UserRegisterRequest;
import com.app.code.model.vo.UserVO;
import com.app.code.util.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.user.client.FileFeignClient;
import com.code.user.service.UserService;
import com.code.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author lfj
* @description 针对表【user(用户信息表)】的数据库操作Service实现
* @createDate 2025-04-25 13:08:03
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;
    @Resource
    private FileFeignClient fileFeignClient;
    @Resource
    private UserService userService;


    /**
     * 用户登录
     */
    public UserVO loginUser(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        password = DigestUtil.md5Hex(password);
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        //更新登录时间
        user.setLastLogin(new Date());
        userService.updateById(user);
        String token = JwtUtils.generateToken(user);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setToken(token);
        return userVO;
    }

    /**
     * 用户注册
     */
    @Override
    public Boolean register(String username, String password, String email, MultipartFile avatarFile, Integer role) {
        if (StringUtils.isAnyEmpty(username, password, email)) {
            throw new BusinessException("参数不能为空");
        }
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        BaseResponse<String> response = fileFeignClient.uploadAvatar(avatarFile);
        if (StringUtils.isNotEmpty(response.getData())){
            user.setAvatarUrl(response.getData());
        }
        user.setUsername(username);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setEmail(email);
        user.setRole(0);
        user.setIsActive(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        boolean flag = save(user);
        if (!flag){
            throw new RuntimeException("注册失败");
        }
        return true;
    }

    /**
     * 管理员删除用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    public Boolean deleteUser(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 根据ID查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 执行删除操作
        boolean result = removeById(userId);
        if (!result) {
            throw new BusinessException("删除用户失败");
        }
        
        return true;
    }
    
    /**
     * 管理员禁用/启用用户
     * @param userId 用户ID
     * @param isActive 是否激活(0-禁用, 1-启用)
     * @return 操作结果
     */
    @Override
    public Boolean updateUserStatus(String userId, Integer isActive) {
        if (StringUtils.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (isActive == null || (isActive != 0 && isActive != 1)) {
            throw new BusinessException("用户状态参数无效");
        }
        
        // 根据ID查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新用户状态
        user.setIsActive(isActive);
        user.setUpdateTime(new Date());
        
        boolean result = updateById(user);
        if (!result) {
            throw new BusinessException("更新用户状态失败");
        }
        
        return true;
    }
    
    /**
     * 管理员重置用户密码
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    public Boolean resetUserPassword(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 根据ID查询用户
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 重置密码为123456
        String defaultPassword = "123456";
        user.setPassword(DigestUtil.md5Hex(defaultPassword));
        user.setUpdateTime(new Date());
        
        boolean result = updateById(user);
        if (!result) {
            throw new BusinessException("重置密码失败");
        }
        
        return true;
    }

    @Override
    public IPage<User> getUserList(UserQueryRequest userQueryRequest) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(userQueryRequest.getKeyword()), User::getUsername, userQueryRequest.getKeyword());
        // 创建分页对象
        Page<User> page = new Page<>(userQueryRequest.getCurrentPage(), userQueryRequest.getPageSize());
        // 执行分页查询
        IPage<User> userPage = this.page(page, queryWrapper);
        // 对结果进行脱敏处理
        userPage.getRecords().forEach(this::getSafetyUser);
        return userPage;
    }


    //用户数据脱敏
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setRole(originUser.getRole());
        safetyUser.setIsActive(originUser.getIsActive());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        safetyUser.setPassword("**********");

        return safetyUser;
    }
}




