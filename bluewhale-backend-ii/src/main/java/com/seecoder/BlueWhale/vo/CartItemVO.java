package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.CartSkuStatus;
import com.seecoder.BlueWhale.po.Cart;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.po.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemVO {
    private Integer skuId;
    private Integer count;
    private CartSkuStatus skuStatus;
    private String skuName;
    private String itemName;
    private Long id;
    private String itemImage;
    private Double total_amount;
    private Long skuInventory;
    private Long skuLimitPerCustomer;
    private String storeName;
    private Integer storeId;
    private Integer itemId;

    public Cart toPO(User user, Sku sku) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCount(getCount());
        cart.setSku(sku);
        cart.setSkuName(sku.getSkuName());
        cart.setItemName(sku.getItem().getItemName());
        if (sku.getSkuInventory() == 0) {
            cart.setSkuStatus(CartSkuStatus.SHORTAGE);
        } else {
            cart.setSkuStatus(CartSkuStatus.NORMAL);
        }
        return cart;
    }
}
