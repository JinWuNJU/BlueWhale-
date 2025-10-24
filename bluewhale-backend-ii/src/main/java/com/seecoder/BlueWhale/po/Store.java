package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.vo.StoreVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = @Index(columnList = "create_time"))
public class Store {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "store_id")
    private Integer storeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logo_url", referencedColumnName = "url")
    private ImageInfo logoImageInfo;
    @Column(name = "store_name", columnDefinition = "varchar(255) CHARACTER SET gb18030 COLLATE gb18030_chinese_ci")
    private String storeName;
    @Column(name = "create_time")
    private Date createTime;
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> item;
    @Column(name = "store_rating", columnDefinition = "double default 0")
    private double storeRating = 0;
    @Column(name = "store_review_count", columnDefinition = "int default 0")
    private int storeReviewCount = 0;

    public void setLogoImageInfo(ImageInfo logoImageInfo) {
        if (this.logoImageInfo != null) {
            this.logoImageInfo.free();
        }
        this.logoImageInfo = logoImageInfo;
        logoImageInfo.use();
    }

    @PrePersist
    private void initialize() {
        setCreateTime(new Date());
    }

    @PreRemove
    public void free() {
        logoImageInfo.free();
    }

    public StoreVO toVO() {
        StoreVO vo = new StoreVO();
        vo.setStoreName(storeName);
        vo.setStoreId(storeId);
        vo.setLogoUrl(getLogoImageInfo().getUrl());
        vo.setStoreRating(Double.parseDouble(String.format("%.2f", getStoreRating())));
        vo.setStoreReviewCount(getStoreReviewCount());
        return vo;
    }
}
