package com.code.file.util;

import com.app.code.exception.BusinessException;
import com.code.file.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author lfj
 * @createDate 2025/4/26
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioConfig minioConfig;


    /**
     * 上传文件到MinIO
     *
     * @param file 文件
     * @param directory 目录
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                throw new BusinessException(400, "上传文件不能为空");
            }

            // 检查文件大小（限制为10MB）
            if (file.getSize() > 50 * 1024 * 1024) {
                throw new BusinessException(400, "上传文件大小不能超过50MB");
            }

            // 检查文件类型
            String contentType = file.getContentType();
//            if (contentType == null || !contentType.startsWith("image/")) {
//                throw new BusinessException(400, "只能上传图片文件");
//            }

            // 检查存储桶是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
            }

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = directory + "/" + fileName;

            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .contentType(contentType)
                            .stream(inputStream, file.getSize(), -1)
                            .build()
            );

            // 关闭输入流
            inputStream.close();

            // 返回文件URL
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            throw new BusinessException(500, "上传文件失败: " + e.getMessage());
        }
    }

    /**
     * 生成文件名
     *
     * @param originalFilename 原始文件名
     * @return 生成的文件名
     */
    private String generateFileName(String originalFilename) {
        // 获取文件扩展名
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成文件名：日期_UUID.扩展名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return date + "_" + uuid + extension;
    }
}
