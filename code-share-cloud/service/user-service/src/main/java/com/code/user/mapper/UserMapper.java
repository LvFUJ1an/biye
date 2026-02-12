package com.code.user.mapper;

import com.app.code.model.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lfj
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2025-04-25 13:08:03
* @Entity com.app.code.model.po.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    User findByUsername(String username);
}




