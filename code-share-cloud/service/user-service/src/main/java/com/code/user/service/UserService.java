package com.code.user.service;

import com.app.code.model.po.User;
import com.app.code.model.request.UserQueryRequest;
import com.app.code.model.request.UserRegisterRequest;
import com.app.code.model.vo.UserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author lfj
* @description 针对表【user(用户信息表)】的数据库操作Service
* @createDate 2025-04-25 13:08:03
*/
public interface UserService extends IService<User> {

    UserVO loginUser(String username, String password);

    Boolean register(String username, String password, String email, MultipartFile avatarFile, Integer role);
    
    /**
     * 管理员删除用户
     * @param userId 用户ID
     * @return 操作结果
     */
    Boolean deleteUser(String userId);
    
    /**
     * 管理员禁用/启用用户
     * @param userId 用户ID
     * @param isActive 是否激活(0-禁用, 1-启用)
     * @return 操作结果
     */
    Boolean updateUserStatus(String userId, Integer isActive);
    
    /**
     * 管理员重置用户密码
     * @param userId 用户ID
     * @return 操作结果
     */
    Boolean resetUserPassword(String userId);

    IPage<User> getUserList(UserQueryRequest userQueryRequest);
}
