package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.OrderStatus;
import com.seecoder.BlueWhale.enums.Shipping;
import com.seecoder.BlueWhale.vo.OrderVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_info", indexes = {
        @Index(columnList = "order_status"),
        @Index(columnList = "shipping"),
        @Index(columnList = "total_amount"),
        @Index(columnList = "count"),
        @Index(columnList = "order_time"),
        @Index(columnList = "trade_no")
})
public class OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    private Item item;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", referencedColumnName = "sku_id")
    private Sku sku;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private Store store;
    @Column(name = "count")
    private int count;
    @Column(name = "shipping")
    @Enumerated(EnumType.STRING)
    private Shipping shipping;
    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "total_amount_original")
    private Double totalAmountOriginal;
    @Column(name = "order_time")
    private Date orderTime;
    @Column(name = "trade_no")
    private String tradeNo;
    @Column(name = "trade_no_ali")
    private String tradeNoAli;
    @Column(name = "store_name")
    private String storeName;
    @Column(name = "item_name")
    private String itemName;
    @Column(name = "sku_name")
    private String skuName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_image", referencedColumnName = "url")
    private ImageInfo itemImage;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "user_phone")
    private String userPhone;
    @Column(name = "user_address")
    private String userAddress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "express", referencedColumnName = "mail_no")
    private ExpressInfo express;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_express", referencedColumnName = "mail_no")
    private ExpressInfo refundExpress;

    @ManyToOne
    @JoinTable(name = "cart_trade_order",
            joinColumns = {@JoinColumn(name = "order_id", referencedColumnName = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "trade_id", referencedColumnName = "id")}
    )
    private CartTrade cartTrade;
    @OneToOne(mappedBy = "orderInfo")
    private Coupon coupon;


    @PrePersist
    private void initialize() {
        setOrderTime(new Date());
        if (getItemImage() != null) {
            getItemImage().use();
        }
    }

    @PreRemove
    private void free() {
        if (getItemImage() != null) {
            getItemImage().free();
        }
    }

    public void setAddress(Address address) {
        setUserAddress(address.getAddress());
        setUserPhone(address.getPhone());
        setUserName(address.getName());
    }

    public OrderVO toVO() {
        OrderVO vo = new OrderVO();
        vo.setSkuId(getSku().getSkuId());
        vo.setCount(getCount());
        vo.setShipping(getShipping());
        vo.setStatus(getOrderStatus());
        vo.setTotal_amount(getTotalAmount());
        vo.setOrderTime(getOrderTime());
        vo.setStoreId(getStore().getStoreId());
        vo.setStoreName(getStoreName());
        vo.setItemName(getItemName());
        vo.setSkuName(getSkuName());
        vo.setItemId(getItem().getItemId());
        vo.setOrderId(getTradeNo());
        vo.setItemImage(getItemImage().getUrl());
        vo.setUserId(getUser().getId());
        vo.setUserName(getUserName());
        vo.setUserPhone(getUserPhone());
        vo.setAddress(getUserAddress());
        vo.setTotal_amount_original(getTotalAmountOriginal());
        if (getExpress() != null)
            vo.setExpressNo(getExpress().getMailNo());
        if (getRefundExpress() != null)
            vo.setRefundExpressNo(getRefundExpress().getMailNo());
        return vo;
    }
}
