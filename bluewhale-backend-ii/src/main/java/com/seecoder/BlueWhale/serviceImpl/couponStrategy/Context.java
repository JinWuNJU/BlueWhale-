package com.seecoder.BlueWhale.serviceImpl.couponStrategy;

import com.seecoder.BlueWhale.enums.CouponType;
import com.seecoder.BlueWhale.exception.BlueWhaleException;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Context {
    private CouponStrategy couponStrategy;

    public double calculate(double price) {
        return couponStrategy.calculate(price);
    }

    public boolean valid(double price) {
        return couponStrategy.valid(price);
    }

    public static Context getContext(CouponGroupVO couponGroup) {
        CouponType couponType = couponGroup.getCouponType();
        if (couponType == CouponType.FULL_REDUCTION) {
            return new Context(new FillReductionCouponCalculateStrategy(couponGroup.getThresholdAmount(), couponGroup.getDiscountAmount()));
        } else if (couponType == CouponType.SPECIAL) {
            return new Context(new SpecialCouponCalculateStrategy());
        }
        throw new BlueWhaleException("未实现的优惠类型");
    }
}
