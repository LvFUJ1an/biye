package com.app.code.model.vo;

import com.app.code.model.po.User;
import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/25
 */
@Data
public class UserVO extends User {
    private String token;
}
