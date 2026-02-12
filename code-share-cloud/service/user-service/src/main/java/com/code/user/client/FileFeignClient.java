package com.code.user.client;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@FeignClient("file-service")
public interface FileFeignClient {

    @PostMapping(value = "/file/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<String> uploadAvatar(@RequestPart("file")MultipartFile file);


}
