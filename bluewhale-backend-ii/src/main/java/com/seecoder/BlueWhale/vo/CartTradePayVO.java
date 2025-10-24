package com.seecoder.BlueWhale.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CartTradePayVO {
    private Set<CartTradeCouponVO> coupon;
}
