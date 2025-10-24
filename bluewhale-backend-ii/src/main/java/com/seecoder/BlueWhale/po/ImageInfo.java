package com.seecoder.BlueWhale.po;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "image_info", indexes = {
        @Index(name = "idx_image_info_ref_count", columnList = "ref_count")
})
public class ImageInfo {
    @Column(name = "ref_count")
    @Basic
    private int refCount = 0;
    @Column(name = "time")
    @Basic
    private Date time;
    @Column(name = "url")
    @Id
    private String url;
    @PrePersist
    public void initialize() {
        setTime(new Date());
    }


    /**
     * 释放对图片资源的引用
     */
    public void free() {
        this.setRefCount(this.getRefCount() - 1);
    }

    /**
     * 增加对图片资源的引用
     */
    public void use() {
        this.setRefCount(this.getRefCount() + 1);
    }
}
