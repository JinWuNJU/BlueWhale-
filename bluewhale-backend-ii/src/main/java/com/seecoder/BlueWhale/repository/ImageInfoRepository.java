package com.seecoder.BlueWhale.repository;

import com.seecoder.BlueWhale.po.ImageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageInfoRepository extends JpaRepository<ImageInfo, String> {
    ImageInfo findByUrl(String url);

    List<ImageInfo> findByRefCount(int count);


    @Query(value = "SELECT * FROM image_info ORDER BY RAND() LIMIT 1", nativeQuery = true)
    ImageInfo findRandomImageInfo();
}
