package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.vo.SkuVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Sku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private int skuId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_image", referencedColumnName = "url", nullable = true)
    private ImageInfo skuImageInfo;
    @ManyToMany
    @JoinTable(
            name = "sku_image_detail_list",
            joinColumns = @JoinColumn(name = "sku_id"),
            inverseJoinColumns = @JoinColumn(name = "url")
    )
    private Set<ImageInfo> skuDetailImage;

    public void setSkuImageInfo(ImageInfo skuImageInfo) {
        if (this.skuImageInfo != null)
            this.skuImageInfo.free();
        this.skuImageInfo = skuImageInfo;
        this.skuImageInfo.use();
    }

    public void setSkuDetailImage(Set<ImageInfo> skuDetailImage) {
        if (this.skuDetailImage != null)
            this.skuDetailImage.forEach(ImageInfo::free);
        this.skuDetailImage = skuDetailImage;
        this.skuDetailImage.forEach(ImageInfo::use);
    }

    @PreRemove
    public void free() {
        if (skuImageInfo != null)
            skuImageInfo.free();
        if (skuDetailImage != null)
            skuDetailImage.forEach(ImageInfo::free);
    }

    @Column(name = "sku_inventory")
    private Long skuInventory;
    @Column(name = "sku_limit_per_customer")
    private Long skuLimitPerCustomer;
    @Column(name = "sku_name", columnDefinition = "varchar(255) CHARACTER SET gb18030 COLLATE gb18030_chinese_ci")
    private String skuName;
    @Column(name = "sku_price")
    private String skuPrice;
    @Column(name = "sku_sold")
    private String skuSold;
    @OneToMany
    private Set<Cart> carts;

    public SkuVO toVO() {
        SkuVO skuVO = new SkuVO();
        skuVO.setSkuId(getSkuId());
        skuVO.setItemId(getItem().getItemId());
        skuVO.setSkuName(getSkuName());
        skuVO.setSkuInventory(getSkuInventory());
        skuVO.setSkuLimitPerCustomer(getSkuLimitPerCustomer());
        skuVO.setSkuPrice(getSkuPrice());
        if (getSkuImageInfo() != null)
            skuVO.setSkuImage(getSkuImageInfo().getUrl());
        if (getSkuDetailImage() != null)
            skuVO.setSkuDetailImage(getSkuDetailImage().stream().map(ImageInfo::getUrl).collect(Collectors.toSet()));
        return skuVO;
    }
}
