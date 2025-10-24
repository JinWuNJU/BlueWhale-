package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.enums.*;
import com.seecoder.BlueWhale.po.OrderInfo;
import com.seecoder.BlueWhale.po.Sku;
import com.seecoder.BlueWhale.po.User;
import com.seecoder.BlueWhale.vo.*;

import java.util.Date;

public interface OrderService {
    ReviewVO review(String orderId, ReviewVO review);


    OrderInfo create(int count, User user, Sku sku, Long addressId, Shipping shipping);

    OrderVO create(OrderPriceCalculateVO order);

    Boolean dispatch(OrderVO order);
    Boolean receive(OrderBasicVO order);

    OrderPriceCalculateVO calculatePrice(boolean recommendCoupon, OrderPriceCalculateVO order, User user);

    OrderPriceCalculateVO calculate(OrderPriceCalculateVO order);

    OrderListVO list(int page, ListSortType sort, SortingOrder sortingOrder, OrderStatus[] orderFilter, Shipping shipping);

    OrderVO get(String orderId);

    TradeStatus payStatus(String orderId);

    OrderInfo payOrder(OrderInfo orderInfo, String couponGroupId, User user);

    String pay(String orderId, String couponInfo);

    void paySucceed(OrderInfo orderInfo, String aliTradeNo);

    void paySucceed(String orderId, Double totalAmount, String aliTradeNo);

    String export(String storeId, Date startTime, Date endTime);

    Boolean cancel(String tradeNo);

    Boolean refund(String orderId, ExpressInfoVO expressInfo);
}
