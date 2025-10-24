package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.po.CartTrade;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.vo.*;

import java.util.Set;

public interface CartService {
    Boolean add(CartItemVO cartItemVO);

    Boolean remove(CartItemListVO cartItemSet);

    Set<CartItemVO> getList();

    CartItemVO put(CartItemVO cartItemVO);

    void skuShortage(Sku sku);

    void skuRestock(Sku sku);

    boolean isCartTradeNo(String tradeNo);

    String createTrade(CartTradeCreateVO cartTradeCreate);

    String pay(String tradeNo, Set<CartTradeCouponVO> usingCoupon);

    void skuRemoval(Sku sku);

    void disableCartTrade(CartTrade cartTrade);

    void paySucceed(String tradeNo, Double totalAmount, String aliTradeNo);

    String getCartTradeNo(CartTrade cartTrade);

    CartTradeCalculateVO calculate(CartItemListVO cartItemListVO);

    CartTradeVO getTrade(String tradeNo, Set<CartTradeCouponVO> usingCoupon);
}
