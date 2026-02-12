package com.code.file.service.impl;

import com.code.file.service.UploadService;
import com.code.file.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@Service
public class UploadServiceImpl implements UploadService {


    @Resource
    private FileUploadUtil fileUploadUtil;


    @Override
    public String uploadFileWithDirectory(MultipartFile file,String directory) {
        return fileUploadUtil.uploadFile(file, directory);
    }

}
