package com.seecoder.BlueWhale.vo;

import com.seecoder.BlueWhale.enums.CouponStatus;
import com.seecoder.BlueWhale.enums.CouponType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CouponVO {
    private String couponDesc;
    private String couponGroupId;
    private String couponName;
    private CouponType couponType;
    private double discountAmount;
    private Date endTime;
    private Date startTime;
    private Integer storeId;
    private double thresholdAmount;
    private CouponStatus couponStatus;
}