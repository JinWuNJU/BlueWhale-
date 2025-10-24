package com.seecoder.BlueWhale.serviceImpl;

import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.ImageInfo;
import com.seecoder.BlueWhale.repository.ImageInfoRepository;
import com.seecoder.BlueWhale.service.ImageService;
import com.seecoder.BlueWhale.util.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: DingXiaoyu
 * @Date: 12:02 2023/12/13
 * 实现了上传文件的功能。
 */
@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Override
    public String upload(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName.endsWith(".jpg")           // 限定后缀为jpg/png/avif，使用ImageIO.read确保上传的文件是图片
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".png")
                    || fileName.endsWith(".avif")
                    || ImageIO.read(file.getInputStream()) == null) {
                String extension = fileName.split("(.*?)(\\.)(?!.*\\.)")[1];
                String sha256 = getSHA256(file.getInputStream());    // 用sha256值作为oss key
                String sha256fileName = sha256 + "." + extension;
                ImageInfo existedImg = imageInfoRepository.findByUrl(ossUtil.getOssUrl() + sha256fileName);
                if (existedImg != null) {
                    return existedImg.getUrl();     // 图片已存在，直接返回链接
                } else {
                    String url = ossUtil.upload(sha256fileName, file.getInputStream());
                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.setUrl(url);
                    imageInfoRepository.save(imageInfo);
                    return url;
                }
            } else {
                throw BlueWhaleException.fileFormatUnknown();
            }
        } catch (BlueWhaleException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw BlueWhaleException.fileUploadFail();
        }
    }

    @Override
    public ImageInfo getImage(String imageUrl) {
        ImageInfo imageInfo = imageInfoRepository.findByUrl(imageUrl);
        if (imageInfo == null) {
            throw BlueWhaleException.imageExpired();
        }
        return imageInfo;
    }

    /**
     * 清理过期的空闲的图片，间隔一天
     * 启动时执行、每天0点执行
     */
    @PostConstruct
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeExpiredImage() {
        long expiredTime = 1000 * 60 * 60 * 24; // 一天
        long now = new Date().getTime();
        List<ImageInfo> images = imageInfoRepository.findByRefCount(0);
        if (!images.isEmpty()) {
            ArrayList<String> toDelete = new ArrayList<>();
            for (ImageInfo freeImg : images) {
                if (freeImg.getTime().getTime() < now - expiredTime) {
                    toDelete.add(freeImg.getUrl());
                    imageInfoRepository.delete(freeImg);
                }
            }
            if (!toDelete.isEmpty()) {
                ossUtil.remove(toDelete);
            }
        }
    }

    private String getSHA256(InputStream inputStream) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                sha256.update(buffer, 0, length);
            }
            byte[] digest = sha256.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            throw BlueWhaleException.fileUploadFail();
        }
    }
}
