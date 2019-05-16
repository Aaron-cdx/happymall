package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author caoduanxi
 * @2019/5/8 21:54
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    //上传文件
    public String upload(MultipartFile file, String path){
        //获取到文件的名称
        String fileName = file.getOriginalFilename();
        //扩展名
        //将文件名分割出来得到扩展名
        String fileExtensionsName = fileName.substring(fileName.lastIndexOf(".")+1);
        //上传的文件名
        //主要是为了避免多个用户上传相同文件名的文件
        //A:abc.jpg
        //B:abc.jpg
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionsName;
        //打印日志
        logger.info("开始上传文件");

        logger.info("开始上传文件，上传文件结束，文件名为:{},上传的路径为:{},新文件名为:{}",fileName,path,uploadFileName);

        //文件夹
        File fileDir = new File(path);
        //如果不存在
        if(!fileDir.exists()){
            //改变文件可写
            fileDir.setWritable(true);
            //创建文件夹
            fileDir.mkdirs();
        }
        //创建目标文件
        File targetFile = new File(path,uploadFileName);
        try {
            //传输文件
            file.transferTo(targetFile);
            //代表传输文件成功  传输文件到FTP服务器
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //删除其中的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
        }
        return targetFile.getName();
    }
}
