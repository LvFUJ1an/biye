package com.code.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author lfj
 * @createDate 2025/4/26
 * 上传业务服务
 */
public interface UploadService {

    String uploadFileWithDirectory(MultipartFile file, String directory);

}
