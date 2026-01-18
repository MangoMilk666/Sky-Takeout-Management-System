package com.sky.service.impl;

import com.sky.service.CommonService;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    /**
     * 将上传的文件保存在本地
     */
    @Override
    public String upload(MultipartFile file) {
        String uploadPath =  "/Users/henrysang/Documents/Sky-Takeout/data/img_data/";
        String accessUrl =  "http://localhost:8080/admin/commoncommon/upload/";

        if (file==null || file.isEmpty()){
            throw new RuntimeException("待上传文件为空");
        }
        try {
            // 生成唯一文件名
            String randomStr = UUID.randomUUID().toString().replaceAll("-", "");
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String newFileName = randomStr + extension;
            // 保存文件
            file.transferTo(new File(uploadPath + newFileName));
            return accessUrl + newFileName;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }
}
