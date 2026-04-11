package com.dreamer.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class AliOSSUtil {

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.backetName}")
    private String backetName;

    public String addAli(MultipartFile file) throws IOException {

        String uploadUrl = null;
            // 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            InputStream inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();
            // 在文件名称中添加随机的唯一的值，防止名称一样时文件的覆盖
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            // 文件类型
            String fileType = filename.substring(filename.lastIndexOf("."));
            filename = uuid + fileType;

            // 把文件安装日期进行分类，会自动创建文件夹
            String datePath = new DateTime().toString("yyyy/MM/dd");
            filename = datePath + "/" + filename;

            ossClient.putObject(backetName, filename, inputStream);

            // 关闭OSSClient
            ossClient.shutdown();

            // 上传文件之后的路径，自己拼接
            uploadUrl = "https://"+backetName+"."+endpoint+"/"+filename;
        return uploadUrl;
    }

}
