package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.CartSkuStatus;
import com.seecoder.BlueWhale.service.OrderService;
import com.seecoder.BlueWhale.vo.CartItemVO;
import com.seecoder.BlueWhale.vo.OrderPriceCalculateVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cart", indexes = {
        @Index(columnList = "sku_id")
})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @Column(name = "count")
    private int count;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sku_id")
    private Sku sku;
    @Column(name = "sku_status")
    @Enumerated(EnumType.STRING)
    private CartSkuStatus skuStatus;
    @Column(name = "sku_name")
    private String skuName;
    @Column(name = "item_name")
    private String itemName;

    public CartItemVO toVO(OrderService orderService) {
        CartItemVO cartItemVO = new CartItemVO();
        Sku sku = getSku();
        cartItemVO.setId(getId());
        cartItemVO.setCount(getCount());
        cartItemVO.setSkuId(sku.getSkuId());
        cartItemVO.setSkuStatus(getSkuStatus());
        cartItemVO.setSkuName(getSkuName());
        cartItemVO.setItemName(getItemName());
        cartItemVO.setSkuInventory(sku.getSkuInventory());
        cartItemVO.setSkuLimitPerCustomer(sku.getSkuLimitPerCustomer());
        Item item = sku.getItem();
        Store store = item.getStore();
        cartItemVO.setStoreName(store.getStoreName());
        cartItemVO.setStoreId(store.getStoreId());
        cartItemVO.setItemId(item.getItemId());
        Optional<ImageInfo> itemImage = sku.getItem().getItemImage().stream().findFirst();
        itemImage.ifPresent(imageInfo -> cartItemVO.setItemImage(itemImage.get().getUrl()));
        if (sku.getSkuInventory() < getCount()) {
            cartItemVO.setSkuStatus(CartSkuStatus.SHORTAGE);
        } else {
            OrderPriceCalculateVO priceVO = new OrderPriceCalculateVO(sku, getCount(), null);
            orderService.calculatePrice(false, priceVO, user);
            cartItemVO.setTotal_amount(priceVO.getTotal_amount());
        }
        return cartItemVO;
    }
}
