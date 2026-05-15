package com.todo.common.util;

import com.aliyun.oss.OSS;
import com.todo.common.config.OssConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    public String upload(MultipartFile file, String subDir) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = ossConfig.getDir() + subDir + "/" + datePath + "/"
                    + UUID.randomUUID().toString().replace("-", "") + extension;

            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
            }

            return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
        } catch (Exception e) {
            log.error("OSS upload failed", e);
            throw new RuntimeException("文件上传失败");
        }
    }
}
