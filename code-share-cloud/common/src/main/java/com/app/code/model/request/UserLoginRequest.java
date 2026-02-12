package com.app.code.model.request;

import lombok.Data;

/**
 * @author lfj
 * @createDate 2025/4/25
 */
@Data
public class UserLoginRequest {
    private String username;
    private String password;
}
