package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.po.ImageInfo;
import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.repository.ImageInfoRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static com.seecoder.BlueWhale.util.NumberUtil.isNumeric;

@Getter
@Setter
@NoArgsConstructor
public class SkuVO {
    private int skuId;
    private int itemId;
    private Long skuInventory;
    private Long skuLimitPerCustomer;
    private String skuName;
    private String skuPrice;
    private String skuImage;
    private Set<String> skuDetailImage;

    public Sku toPO(Item item, ImageInfoRepository imageInfoRepository) {
        Sku sku = new Sku();
        sku.setItem(item);
        sku.setSkuName(skuName);
        sku.setSkuPrice(skuPrice);
        sku.setSkuLimitPerCustomer(skuLimitPerCustomer);
        if (skuImage != null) {
            ImageInfo img = imageInfoRepository.findByUrl(skuImage);
            if (img != null)
                sku.setSkuImageInfo(img);
            else
                throw BlueWhaleException.imageExpired();
        }
        if (skuDetailImage != null && !skuDetailImage.isEmpty()) {
            HashSet<ImageInfo> skuDetailImageInfo = new HashSet<>();
            for (String imgUrl : skuDetailImage) {
                ImageInfo img = imageInfoRepository.findByUrl(imgUrl);
                if (img != null)
                    skuDetailImageInfo.add(img);
                else
                    throw BlueWhaleException.imageExpired();
            }
            sku.setSkuDetailImage(skuDetailImageInfo);
        }
        sku.setSkuInventory(skuInventory);
        return sku;
    }

    public boolean validForCreate() {
        return skuInventory != null
                && skuLimitPerCustomer != null
                && skuName != null
                && skuPrice != null && isNumeric(skuPrice);
    }
}
