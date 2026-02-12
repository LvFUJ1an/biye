package com.code.file.controller;

import com.app.code.common.BaseResponse;
import com.app.code.common.ResultUtils;
import com.code.file.service.UploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@RestController
@RequestMapping("/file")
public class UploadController {

    @Resource
    private UploadService uploadService;

    //上传用户头像
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<String> uploadAvatar(@RequestPart("file")MultipartFile file) {
        return ResultUtils.success(uploadService.uploadFileWithDirectory(file, "avatar"));
    }


    //上传博客内容图片
    @PostMapping(value = "/blogImage")
    public BaseResponse<String> blogImage(@RequestPart("file")MultipartFile file){
        return ResultUtils.success(uploadService.uploadFileWithDirectory(file, "blogImage"));
    }


    @PostMapping(value = "/uploadAny")
    public BaseResponse<String> uploadAny(@RequestPart("file")MultipartFile file, @RequestParam("directory") String directory){
        return ResultUtils.success(uploadService.uploadFileWithDirectory(file, directory));
    }






}
