package com.seecoder.BlueWhale.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seecoder.BlueWhale.po.Coupon;
import com.seecoder.BlueWhale.po.OrderInfo;
import com.seecoder.BlueWhale.po.Sku;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 同时作为向前端返回订单+优惠券价格的view object，以及在价格计算过程中作为存储结果的对象
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderPriceCalculateVO extends OrderBasicVO {
    private String couponGroupId;
    @JsonIgnore
    private Coupon usingCouponPO;  // 供价格计算使用
    @JsonIgnore
    private Sku sku; // 供价格计算使用
    private String maxDiscountCoupon;
    private Double total_amount_max_discount;
    private List<CouponGroupVO> couponGroup;

    public OrderPriceCalculateVO(Sku sku, int count, String couponGroupId) {
        setSku(sku);
        setSkuId(sku.getSkuId());
        setCouponGroupId(couponGroupId);
        setCount(count);
        setTotal_amount_original(count * Double.parseDouble(sku.getSkuPrice()));
        setTotal_amount(getTotal_amount_original());
    }

    public OrderPriceCalculateVO(OrderInfo order) {
        this(order.getSku(), order.getCount(), null);
        setOrderId(order.getTradeNo());
    }
}
