package com.code.blog.controller;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.Ebook;
import com.app.code.model.po.User;
import com.app.code.model.po.Videos;
import com.app.code.model.request.EBookQueryRequest;
import com.app.code.model.request.EBookStatusRequest;
import com.app.code.util.UserUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.blog.client.FileFeignClient;
import com.code.blog.service.EbookService;
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
@RequestMapping("/ebook")
public class EBookController {

    @Resource
    private EbookService eBookService;

    @Resource
    private FileFeignClient fileFeignClient;

    @PostMapping("/page")
    public BaseResponse<Page<Ebook>> pageQuery(@RequestBody EBookQueryRequest eBookQueryRequest, HttpServletRequest request) {
        String userInfo = request.getHeader("X-User-Info");
        User user = UserUtil.getUser(userInfo);
        Page<Ebook> page = eBookService.pageQuery(eBookQueryRequest,user);
        return BaseResponse.success(page);
    }

    @PostMapping("modify")
    public BaseResponse<Boolean> updateEbook(@RequestParam("id") Integer id,
                                             @RequestParam("title") String title,
                                             @RequestParam("description") String description,
                                             @RequestParam("image") MultipartFile image,
                                             @RequestParam("ebookFile") MultipartFile ebookFile,
                                             @RequestParam("keepOriginalImage") Boolean keepOriginalImage,
                                             @RequestParam("keepOriginalFile") Boolean keepOriginalFile) {
        if (id == null){
            return BaseResponse.error(400, "参数不能为空");
        }
        Ebook ebook = eBookService.getById(id);
        if (StringUtils.isAnyEmpty(title, description)){
            return BaseResponse.error(400, "参数不能为空");
        }
        if (!keepOriginalFile){
            String EbookFileUrl = fileFeignClient.uploadAny(ebookFile,"EbookFile").getData();
            ebook.setFilePath(EbookFileUrl);
        }
        if (!keepOriginalImage){
            String EbookImageUrl = fileFeignClient.uploadAny(image,"EbookImage").getData();
            ebook.setImage(EbookImageUrl);
        }
        //处理上传
        if (StringUtils.isNotBlank(title)){
            ebook.setTitle(title);
        }
        if (StringUtils.isNotBlank(description)){
            ebook.setDescription(description);
        }
        ebook.setUpdateTime(new java.util.Date());
        boolean save = eBookService.updateById(ebook);
        return BaseResponse.success(save);
    }

    @PostMapping("/create")
    public BaseResponse<Boolean> createEbook(@RequestParam("title") String title,
                                             @RequestParam("description") String description,
                                             @RequestParam("image") MultipartFile image,
                                             @RequestParam("ebookFile") MultipartFile ebookFile) {
        if (StringUtils.isAnyEmpty(title, description)){
            return BaseResponse.error(400, "参数不能为空");
        }
        if (ebookFile == null){
            return BaseResponse.error(400, "文件不能为空");
        }
        if (image == null){
            return BaseResponse.error(400, "封面不能为空");
        }
        //处理上传
        String EbookImageUrl = fileFeignClient.uploadAny(image,"EbookImage").getData();
        String EbookFileUrl = fileFeignClient.uploadAny(ebookFile,"EbookFile").getData();
        Ebook ebook = new Ebook();
        ebook.setTitle(title);
        ebook.setDescription(description);
        ebook.setImage(EbookImageUrl);
        ebook.setFilePath(EbookFileUrl);
        ebook.setIsPublished(0);
        ebook.setCreateTime(new java.util.Date());
        ebook.setUpdateTime(new java.util.Date());
        boolean save = eBookService.save(ebook);
        return BaseResponse.success(save);
    }

    @DeleteMapping("/delete/{id}")
    public BaseResponse<Boolean> deleteEbook(@PathVariable("id") Integer id) {
        boolean delete = eBookService.removeById(id);
        return BaseResponse.success(delete);
    }

    @PostMapping("/status")
    public BaseResponse<Boolean> updateEbookStatus(@RequestBody EBookStatusRequest ebookStatusRequest) {
        Ebook ebook = eBookService.getById(ebookStatusRequest.getEBookId());
        if (ebook == null){
            return BaseResponse.error(400, "电子书不存在");
        }
        ebook.setIsPublished(ebookStatusRequest.getIsPublished());
        ebook.setUpdateTime(new java.util.Date());
        boolean save = eBookService.updateById(ebook);
        return BaseResponse.success(save);
    }


    @GetMapping("/getEbookById/{id}")
    public BaseResponse<Ebook> getEbookById(@PathVariable("id") Integer id) {
        Ebook ebook = eBookService.getById(id);
        if (ebook == null){
            return BaseResponse.error(400, "电子书不存在");
        }
        ebook.setViewCount(ebook.getViewCount() + 1);
        eBookService.updateById(ebook);
        return BaseResponse.success(ebook);
    }


}
