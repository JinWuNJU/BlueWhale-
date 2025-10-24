package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.po.ImageInfo;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String upload(MultipartFile file);

    ImageInfo getImage(String imageUrl);
}
