package com.seecoder.BlueWhale.service;

import com.seecoder.BlueWhale.enums.CouponListSortType;
import com.seecoder.BlueWhale.po.Coupon;
import com.seecoder.BlueWhale.po.OrderInfo;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import com.seecoder.BlueWhale.vo.CouponGroupListVO;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import com.seecoder.BlueWhale.vo.CouponVO;
import com.seecoder.BlueWhale.vo.OrderPriceCalculateVO;

import java.util.List;
import java.util.Set;

public interface CouponService {
    Coupon getCouponByIdForCustomer(String couponGroupId, User user);

    CouponGroupVO createGroup(CouponGroupVO groupVO);

    void useCoupon(Coupon coupon, OrderInfo orderInfo);

    boolean isValidTime(Coupon coupon);

    CouponVO claim(String couponGroupId);

    List<CouponGroupVO> getCouponGroupsOfStoreForCustomer(Set<Store> stores, User user);

    CouponGroupListVO list(int page, Integer storeId, CouponListSortType sort);

    void refundCoupon(OrderInfo orderInfo);

    Coupon getCouponThatIsValid(OrderPriceCalculateVO order, User user);

    double applyCouponOnPrice(Coupon coupon, double price);

    List<OrderPriceCalculateVO> getCouponListForOrders(List<OrderPriceCalculateVO> orders, User user);
}
