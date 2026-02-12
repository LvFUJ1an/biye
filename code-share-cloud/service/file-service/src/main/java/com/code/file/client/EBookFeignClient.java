package com.code.file.client;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.Ebook;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lfj
 * @createDate 2025/5/4
 */
@FeignClient(name = "blog-service")
public interface EBookFeignClient {
    @GetMapping("/ebook/getEbookById/{id}")
    BaseResponse<Ebook> getEbookById(@PathVariable("id") Integer id);
}
