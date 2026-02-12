package com.code.blog.client;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@FeignClient("user-service")
public interface UserFeignClient {

    @GetMapping("/user/getUserById")
    BaseResponse<User> getUserById(@RequestParam("userId") Integer userId);

    @PostMapping("/user/getUserByListId")
    BaseResponse<List<User>> getUserByListId(@RequestBody List<Integer> userIdList);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    @GetMapping("/user/count")
    BaseResponse<Integer> getUserCount();
    
    /**
     * 获取本月每天的新增用户数
     * @return 本月每天的新增用户数，格式为：{"日期1": 数量1, "日期2": 数量2, ...}
     */
    @GetMapping("/user/countByDayInCurrentMonth")
    BaseResponse<Map<String, Integer>> getUserCountByDayInCurrentMonth();
}
