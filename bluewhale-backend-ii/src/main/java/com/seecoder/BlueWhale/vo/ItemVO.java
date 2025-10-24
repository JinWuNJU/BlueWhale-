package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.po.Item;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.repository.ImageInfoRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ItemVO extends ItemBasicVO {

    private Date itemCreateTime;
    private String itemSoldInfo;
    private Set<String> itemDescImage;
    private Set<SkuVO> itemSku;

    public Item toPO(Store store, ImageInfoRepository imageRepo) {
        Item item = new Item();
        item.setStore(store);
        item.setItemName(itemName);
        item.setItemDesc(itemDesc);
        item.setItemPrice(itemPrice);
        item.setItemCategory(itemCategory);
        item.setItemImage(itemImage.stream().map(imageRepo::findByUrl).collect(Collectors.toSet()));
        item.setSku(itemSku.stream().map(v -> v.toPO(item, imageRepo)).collect(Collectors.toSet()));
        if (itemDescImage != null && !itemDescImage.isEmpty())
            item.setItemDescImage(itemDescImage.stream().map(imageRepo::findByUrl).collect(Collectors.toSet()));
        return item;
    }

    public void fromBasic(ItemBasicVO vo) {
        if (vo != null) {
            setStoreId(vo.getStoreId());
            setItemId(vo.getItemId());
            setItemName(vo.getItemName());
            setItemDesc(vo.getItemDesc());
            setItemPrice(vo.getItemPrice());
            setItemCategory(vo.getItemCategory());
            setItemImage(vo.getItemImage());
            setItemRating(vo.getItemRating());
            setItemReviewCount(vo.getItemReviewCount());
        }
    }

    @Override
    public boolean validForCreate() {
        if(super.validForCreate() && itemSku != null && !itemSku.isEmpty()) {
            for (SkuVO sku : itemSku) {
                if(!sku.validForCreate()) return false;
            }
            return true;
        }
        return false;
    }
}
