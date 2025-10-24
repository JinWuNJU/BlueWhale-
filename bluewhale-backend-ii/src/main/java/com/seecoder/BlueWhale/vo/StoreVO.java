package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.po.ImageInfo;
import com.seecoder.BlueWhale.po.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreVO {
    private Integer storeId;
    private String storeName;
    private String logoUrl;
    private double storeRating;
    private int storeReviewCount;

    public Store toPO(ImageInfo logoImageInfo) {
        Store store = new Store();
        store.setStoreName(storeName);
        store.setLogoImageInfo(logoImageInfo);
        return store;
    }
}
