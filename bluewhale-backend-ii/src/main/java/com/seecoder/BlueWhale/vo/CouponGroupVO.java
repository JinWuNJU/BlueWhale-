package com.seecoder.BlueWhale.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seecoder.BlueWhale.enums.CouponStatus;
import com.seecoder.BlueWhale.enums.CouponType;
import com.seecoder.BlueWhale.po.CouponGroup;
import com.seecoder.BlueWhale.po.Store;
import com.seecoder.BlueWhale.po.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class CouponGroupVO implements Cloneable{
    private String couponDesc;
    private String couponGroupId;
    private String couponName;
    private String storeName;
    private CouponType couponType;
    private double discountAmount;
    private Date endTime;
    private Date startTime;
    private Integer storeId;
    private int usedCount;
    private int totalCount;
    private int remainingCount;
    private double thresholdAmount;
    private CouponStatus couponStatus; // 顾客视角
    @JsonIgnore
    private double discountAmountForOrder; // 用于记录优惠券使用到订单后的折扣

    @Override
    public CouponGroupVO clone(){
        try {
            return (CouponGroupVO) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }

    
    public void omitManagementData() {
        setUsedCount(0);
        setTotalCount(0);
        setRemainingCount(0);
    }

    public CouponGroup toPO(Store store, User user) {
        CouponGroup couponGroup = new CouponGroup();
        if (store != null) {
            couponGroup.setStore(store);
            couponGroup.setStoreName(store.getStoreName());
        }
        couponGroup.setUser(user);
        couponGroup.setTotalCount(getTotalCount());
        couponGroup.setRemainingCount(getTotalCount());
        if (getStartTime() == null)
            couponGroup.setStartTime(new Date());
        else
            couponGroup.setStartTime(getStartTime());
        if (getEndTime() == null)
            couponGroup.setAslongTermCoupon();
        else
            couponGroup.setEndTime(getEndTime());
        couponGroup.setCouponType(getCouponType());
        couponGroup.setCouponName(getCouponName());
        couponGroup.setCouponDesc(getCouponDesc());
        if (couponGroup.getCouponType() == CouponType.FULL_REDUCTION) {
            couponGroup.setThresholdAmount(getThresholdAmount());
            couponGroup.setDiscountAmount(getDiscountAmount());
        }
        return couponGroup;
    }
}
