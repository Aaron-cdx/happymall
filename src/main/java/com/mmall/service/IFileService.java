package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author caoduanxi
 * @2019/5/8 21:54
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
