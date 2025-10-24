package com.seecoder.BlueWhale.po;

import com.seecoder.BlueWhale.enums.CouponStatus;
import com.seecoder.BlueWhale.vo.CouponGroupVO;
import com.seecoder.BlueWhale.vo.CouponVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coupon", indexes = {
        @Index(columnList = "coupon_status"),
        @Index(columnList = "disabled_time")
})
public class Coupon {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    private Store store;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private long couponId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "coupon_group_id", referencedColumnName = "coupon_group_id")
    private CouponGroup couponGroup;
    @Column(name = "coupon_status")
    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;
    @Column(name = "disabled_time")
    private Date disabledTime;
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private OrderInfo orderInfo;

    public CouponVO toVO() {
        CouponVO couponVO = new CouponVO();
        couponVO.setCouponStatus(getCouponStatus());
        CouponGroupVO couponGroupVO = getCouponGroup().toVO();
        couponVO.setCouponGroupId(couponGroupVO.getCouponGroupId());
        couponVO.setCouponName(couponGroupVO.getCouponName());
        couponVO.setCouponDesc(couponGroupVO.getCouponDesc());
        couponVO.setCouponType(couponGroupVO.getCouponType());
        couponVO.setDiscountAmount(couponGroupVO.getDiscountAmount());
        couponVO.setThresholdAmount(couponGroupVO.getThresholdAmount());
        couponVO.setStartTime(couponGroupVO.getStartTime());
        couponVO.setEndTime(couponGroupVO.getEndTime());
        couponVO.setStoreId(couponGroupVO.getStoreId());
        return couponVO;
    }
}
