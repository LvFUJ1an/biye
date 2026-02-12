package com.app.code.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author lfj
 * @createDate 2025/4/25
 */
@Data
public class UserRegisterRequest {
    private MultipartFile avatarFile;
    private String username;
    private String password;
    private String email;
    private String avatarUrl;
    private Integer role;
}
