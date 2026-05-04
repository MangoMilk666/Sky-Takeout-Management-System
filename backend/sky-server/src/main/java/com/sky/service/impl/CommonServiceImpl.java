package com.sky.service.impl;

import com.sky.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Value("${sky.upload-path:/app/data/img_data/}")
    private String uploadPath;

    @Value("${sky.upload-access-url-prefix:/admin/common/upload/}")
    private String uploadAccessUrlPrefix;

    /**
     * 将上传的文件保存在本地
     */
    @Override
    public String upload(MultipartFile file) {
        String normalizedUploadPath = uploadPath.endsWith("/") ? uploadPath : (uploadPath + "/");
        String normalizedAccessUrlPrefix = uploadAccessUrlPrefix.endsWith("/")
                ? uploadAccessUrlPrefix
                : (uploadAccessUrlPrefix + "/");

        if (file==null || file.isEmpty()){
            throw new RuntimeException("待上传文件为空");
        }
        try {
            // 生成唯一文件名
            String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String newFileName = randomStr + extension;
            // 保存文件
            file.transferTo(new File(normalizedUploadPath + newFileName));
            return normalizedAccessUrlPrefix + newFileName;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }
}
