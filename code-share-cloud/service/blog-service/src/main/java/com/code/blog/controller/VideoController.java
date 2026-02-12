package com.code.blog.controller;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.User;
import com.app.code.model.po.Videos;
import com.app.code.model.request.VideoQueryRequest;
import com.app.code.model.request.VideoStatusRequest;
import com.app.code.util.UserUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.client.FileFeignClient;
import com.code.blog.client.UserFeignClient;
import com.code.blog.service.VideosService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author lfj
 * @createDate 2025/4/29
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private VideosService videosService;

    @Resource
    private FileFeignClient fileFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @PostMapping("/page")
    public BaseResponse<Page<Videos>> pageQuery(@RequestBody VideoQueryRequest videoQueryRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        Page<Videos> page = videosService.pageQuery(videoQueryRequest,user);
        return BaseResponse.success(page);
    }

    @PostMapping("modify")
    public BaseResponse<Boolean> updateEbook(@RequestParam("id") Integer id,
                                             @RequestParam("title") String title,
                                             @RequestParam("description") String description,
                                             @RequestParam(value = "image",required = false) MultipartFile image,
                                             @RequestParam(value = "videoFile",required = false) MultipartFile videoFile,
                                             @RequestParam("keepOriginalImage") Boolean keepOriginalImage,
                                             @RequestParam("keepOriginalFile") Boolean keepOriginalFile,
                                             @RequestParam("userId") Integer userId) {
        if (id == null){
            return BaseResponse.error(400, "参数不能为空");
        }
        Videos video = videosService.getById(id);
        if (StringUtils.isAnyEmpty(title, description)){
            return BaseResponse.error(400, "参数不能为空");
        }
        if (!keepOriginalFile){
            String EbookFileUrl = fileFeignClient.uploadAny(videoFile,"videoFile").getData();
            video.setVideoPath(EbookFileUrl);
        }
        if (!keepOriginalImage){
            String coverPath = fileFeignClient.uploadAny(image,"videoImage").getData();
            video.setCoverPath(coverPath);
        }
        //处理上传
        if (StringUtils.isNotBlank(title)){
            video.setTitle(title);
        }
        if (StringUtils.isNotBlank(description)){
            video.setDescription(description);
        }
        video.setUserId(userId);
        video.setUpdateTime(new java.util.Date());
        boolean save = videosService.updateById(video);
        return BaseResponse.success(save);
    }

    @PostMapping("/create")
    public BaseResponse<Boolean> createEbook(@RequestParam("title") String title,
                                             @RequestParam("description") String description,
                                             @RequestParam("image") MultipartFile image,
                                             @RequestParam("videoFile") MultipartFile videoFile,
                                                                    HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        if (StringUtils.isAnyEmpty(title, description)){
            return BaseResponse.error(400, "参数不能为空");
        }
        if (videoFile == null){
            return BaseResponse.error(400, "文件不能为空");
        }
        if (image == null){
            return BaseResponse.error(400, "封面不能为空");
        }
        //处理上传
        String videoImageUrl = fileFeignClient.uploadAny(image,"videoFile").getData();
        String videoFileUrl = fileFeignClient.uploadAny(videoFile,"videoImage").getData();
        Videos video = new Videos();
        video.setTitle(title);
        video.setDescription(description);
        video.setCoverPath(videoImageUrl);
        video.setVideoPath(videoFileUrl);
        video.setIsPublished(0);
        video.setUserId(user.getId());
        video.setCreateTime(new java.util.Date());
        video.setUpdateTime(new java.util.Date());
        boolean save = videosService.save(video);
        return BaseResponse.success(save);
    }

    @PostMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteEbook(@PathVariable("id") Integer id) {
        boolean delete = videosService.removeById(id);
        return BaseResponse.success(delete);
    }

    @PostMapping("/status")
    public BaseResponse<Boolean> updateEbookStatus(@RequestBody VideoStatusRequest videoStatusRequest) {
        Videos video = videosService.getById(videoStatusRequest.getVideoId());
        if (video == null){
            return BaseResponse.error(400, "电子书不存在");
        }
        video.setIsPublished(videoStatusRequest.getIsPublished());
        video.setUpdateTime(new java.util.Date());
        boolean save = videosService.updateById(video);
        return BaseResponse.success(save);
    }

    @PostMapping("/getVideoById/{id}")
    public BaseResponse<Videos> getVideoById(@PathVariable("id") Integer id) {
        Videos video = videosService.getById(id);
        Integer viewCount = video.getViewCount();
        video.setViewCount(viewCount + 1);
        videosService.updateById(video);
        video.setUserName(userFeignClient.getUserById(video.getUserId()).getData().getUsername());
        video.setAvatarUrl(userFeignClient.getUserById(video.getUserId()).getData().getAvatarUrl());
        return BaseResponse.success(video);
    }
}
