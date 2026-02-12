package com.app.code.util;

import com.alibaba.fastjson.JSONObject;
import com.app.code.model.po.User;
import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
public class UserUtil {

    public static User getUser(String json){
        return JSONObject.parseObject(json, User.class);
    }
}
