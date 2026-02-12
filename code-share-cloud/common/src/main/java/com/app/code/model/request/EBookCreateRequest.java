package com.app.code.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
@Data
public class EBookCreateRequest {
    private String title;
    private String description;
    private MultipartFile image;
    private MultipartFile filePath;
}
