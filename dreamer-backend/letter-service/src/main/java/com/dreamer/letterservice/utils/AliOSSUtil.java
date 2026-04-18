package com.dreamer.letterservice.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Component
public class AliOSSUtil {

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

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

            ossClient.putObject(bucketName, filename, inputStream);

            // 关闭OSSClient
            ossClient.shutdown();

            // 上传文件之后的路径，自己拼接
            uploadUrl = "https://"+bucketName+"."+endpoint+"/"+filename;
        return uploadUrl;
    }

    /**
     * 从 OSS 的 URL 中提取 objectName
     * 例如：https://bucket.oss-cn-hangzhou.aliyuncs.com/a/b/c.txt → a/b/c.txt
     */
    public String getObjectNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // 去掉最前面的 /
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    public void removeAli(String fileUrl) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        URL url;
        String path;
        try {
            url = new URL(fileUrl);
            path = url.getPath();
            // 去掉最前面的 /
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // 删除文件或目录。如果要删除目录，目录必须为空。
            ossClient.deleteObject(bucketName, path);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
