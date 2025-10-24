package com.seecoder.BlueWhale.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DingXiaoyu
 * @Date: 12:00 2023/12/13
 * 这个类实现了文件上传。
 * @ConfigurationProperties注解能够读取配置文件。
 * 利用了阿里云OSS服务。
*/
@Component
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aliyun.oss")
public class OssUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    public String getOssUrl() {
        return "http://" + bucketName + "." + endpoint + "/";
    }
    public String upload(String objectName, InputStream inputStream) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setCacheControl("public");
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream, objectMetadata);
        try {
            ossClient.putObject(putObjectRequest);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return ossClient.generatePresignedUrl(bucketName, objectName, new Date()).toString().split("\\?Expires")[0];
    }

    /**
     * 删除oss上的图片
     * @param urls 完整的url，含http://
     */
    public void remove(List<String> urls) {
        String ossUrl = getOssUrl();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ArrayList<String> fileName = new ArrayList<>();
        for (String url : urls) {
            fileName.add(url.replace(ossUrl, ""));
        }
        try {
            ossClient.deleteObjects(new DeleteObjectsRequest(bucketName).withKeys(fileName).withEncodingType("url"));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}

