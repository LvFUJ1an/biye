package com.code.file.controller;

import com.app.code.common.BaseResponse;
import com.app.code.model.po.Ebook;
import com.code.file.client.EBookFeignClient;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resources;
import java.io.InputStream;

/**
 * @author lfj
 * @createDate 2025/4/30
 */
@RestController
@RequestMapping("/file/download")
public class DownloadController {


    @Value("${minio.bucketName}")
    private String bucketName;
    @javax.annotation.Resource
    private MinioClient minioClient;
    @javax.annotation.Resource
    private EBookFeignClient eBookFeignClient;


    @PostMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer id) {
        BaseResponse<Ebook> response = eBookFeignClient.getEbookById(id);
        if (response.getData()==null){
            throw new RuntimeException("文件不存在");
        }
        String filename = response.getData().getFilePath();
        if (filename==null){
            throw new RuntimeException("文件不存在");
        }
        filename = filename.substring(filename.lastIndexOf("/")+1);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object("/EbookFile/" + filename)
                            .build());

            ByteArrayResource resource = new ByteArrayResource(IOUtils.toByteArray(stream));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
