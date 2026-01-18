package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    /**
     * 将上传的文件保存在本地
     */
    String upload(MultipartFile file);
}
