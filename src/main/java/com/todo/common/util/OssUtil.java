package com.todo.common.util;

import com.aliyun.oss.OSS;
import com.todo.common.api.ResponseCode;
import com.todo.common.config.OssConfig;
import com.todo.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    public String upload(MultipartFile file, String subDir) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "文件大小不能超过2MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "仅支持 jpg、png、gif、webp 格式");
        }

        try {
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
