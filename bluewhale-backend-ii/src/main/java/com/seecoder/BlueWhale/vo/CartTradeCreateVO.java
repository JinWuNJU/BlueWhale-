package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.Shipping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 购物车交易信息
 */
@Getter
@Setter
@NoArgsConstructor
public class CartTradeCreateVO extends CartItemListVO {
    private Shipping shipping;
    private Long addressId;
}
