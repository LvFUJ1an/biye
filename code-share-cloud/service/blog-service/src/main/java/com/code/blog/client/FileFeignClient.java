package com.code.blog.client;

import com.app.code.common.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@FeignClient("file-service")
public interface FileFeignClient {

    @PostMapping(value = "/file/uploadAny", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<String> uploadAny(@RequestPart("file")MultipartFile file, @RequestParam("directory") String directory);
}
