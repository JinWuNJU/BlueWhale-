package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.ItemCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

/**
 * 商品的基本描述，用于商品列表等粗略浏览的地方
 */
@Getter
@Setter
@NoArgsConstructor
public class ItemBasicVO {

    protected int storeId;
    protected int itemId;
    protected String itemName;
    protected String itemDesc;
    protected String itemPrice;
    protected ItemCategory itemCategory;
    protected Set<String> itemImage;
    protected double itemRating = 0;
    protected int itemReviewCount = 0;

    public boolean validForCreate() {
        return itemName != null &&
                itemDesc != null &&
                itemImage != null && itemImage.size() >= 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemBasicVO)) {
            return false;
        }
        ItemBasicVO that = (ItemBasicVO) o;
        return getStoreId() == that.getStoreId() && getItemId() == that.getItemId() && Double.compare(getItemRating(), that.getItemRating()) == 0 && getItemReviewCount() == that.getItemReviewCount() && Objects.equals(getItemName(), that.getItemName()) && Objects.equals(getItemDesc(), that.getItemDesc()) && Objects.equals(getItemPrice(), that.getItemPrice()) && getItemCategory() == that.getItemCategory() && Objects.equals(getItemImage(), that.getItemImage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStoreId(), getItemId(), getItemName(), getItemDesc(), getItemPrice(), getItemCategory(), getItemImage(), getItemRating(), getItemReviewCount());
    }
}
